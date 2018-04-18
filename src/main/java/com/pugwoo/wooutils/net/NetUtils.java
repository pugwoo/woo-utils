package com.pugwoo.wooutils.net;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

public class NetUtils {
	
	/**
	 * 一种简单的CSRF检查方案，用于Spring MVC框架
	 * 背景：（鉴于每个请求带csrfToken的方式成本过高，前后端依赖加重）
	 *       （为每个请求指定只有POST方法支持，可以有效防御CSRF，但容易人为疏忽忘加，其次每个接口都加上也增加了工作量）。
	 * 方案：对于api接口（判断方式是接口方法注解了ResponseBody或类注解了RestController），
	 *       判断其Referer是否有值且(和目标api同域名或属于白名单域名)，
	 *       否则csrf校验不通过。
	 * @param request
	 * @param handler spring mvc拦截器的handler参数，只处理HandlerMethod类型
	 * @param whiteDomains 域名白名单，只有系统涉及到跨域才需要设置这个，**不需要带协议和端口**，不检查这两者，例如www.abc.com或112.123.234.456
	 * @return 返回true表示csrf校验通过，返回false为不通过
	 */
	public static boolean csrfPassed(HttpServletRequest request, Object handler, String... whiteDomains) {
		if(!(handler instanceof HandlerMethod)) {
			return true;
		}
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		boolean isResponseBody = handlerMethod.getMethodAnnotation(ResponseBody.class) != null;
		boolean isRestController = false;
		try {
			Class<?> clazz = Class.forName("org.springframework.web.bind.annotation.RestController");
			if(clazz != null) {
				isRestController = handlerMethod.getBeanType().getAnnotation(RestController.class) != null;
			}
		} catch (Exception e) { // RestController is since spring mvc 4, so if it exists
		}
		if(!(isResponseBody || isRestController)) {
			return true;
		}
		
		String referer = request.getHeader("Referer");
		if(referer == null || referer.trim().isEmpty()) {
			return false;
		}
		
		String hostname = NetUtils.getHostname(request);
		if(whiteDomains != null && whiteDomains.length > 0) {
			for(String domain : whiteDomains) {
				if(hostname.equalsIgnoreCase(domain)) {
					return true;
				}
			}
		}
		
		String urlHostname = NetUtils.getUrlHostname(referer);
		if(urlHostname.equalsIgnoreCase(hostname)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 获得本机的ipv4的所有ip列表，排除本机ip 127.开头的
	 * @throws SocketException 
	 */
	public static List<String> getIpv4IPs() throws SocketException {
		List<String> ips = new ArrayList<String>();
		String regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
		Pattern pattern = Pattern.compile(regex);

		for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
			NetworkInterface iface = ifaces.nextElement();
			for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses.hasMoreElements();) {
				InetAddress address = addresses.nextElement();
				if (pattern.matcher(address.getHostAddress()).find() && !address.getHostAddress().startsWith("127.")) {
					ips.add(address.getHostAddress());
				}
			}
		}

		return ips;
	}

	/**
	 * 获得客户端的ip地址，请配合nginx配置使用
	 * @return
	 */
	public static String getRemoteIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if(ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 判断是否在微信浏览器访问
	 * @param request
	 * @return
	 */
	public static boolean isWeixinBrowser(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		if(userAgent != null && userAgent.toLowerCase().contains("micromessenger")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断是否是移动端浏览器
	 * @param request
	 * @return
	 */
	public static boolean isMobileBrowser(HttpServletRequest request) {
		String userAgent = request.getHeader("user-agent");
		if(userAgent != null && userAgent.contains("Mobile")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获得访问的url的域名部分
	 * @param request
	 * @return 例如http://www.abc.com，不带根/
	 */
	public static String getHttpRootURL(HttpServletRequest request) {
		return request.getScheme() + "://" + request.getServerName()
				+ ("http".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 80
				|| "https".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 443 ? ""
					: ":" + request.getServerPort());
	}
	
	/**
	 * 获得当前域名，不带端口，例如www.abc.com
	 * @param request
	 * @return
	 */
	public static String getHostname(HttpServletRequest request) {
		return request.getServerName();
	}
	
	/**
	 * 获得当前域名和端口，如果是http协议，返回www.abc.com:80，如果是https协议，返回www.abc.com:443
	 * 其它端口正常带上，例如www.abc.com:8080
	 * @param request
	 * @return
	 */
	public static String getHostnameWithPort(HttpServletRequest request) {
		return request.getServerName() + ":" + request.getServerPort();
	}
	
	/**
	 * 获得当前请求的完整地址
	 * @param request
	 * @return
	 */
	public static String getFullUrlWithParam(HttpServletRequest request) {
		String domain = getHttpRootURL(request);
		String path = request.getRequestURI();
		String queryString = request.getQueryString();
		return domain + path + (queryString == null ? "" : "?" + queryString);
	}
	
	/**
	 * 获得url的路径，例如访问url是http://www.abc.com/is/a/apple?id=3，则返回/is/a/apple
	 * @param request
	 * @return
	 */
	public static String getUrlPath(HttpServletRequest request) {
		return request.getRequestURI();
	}
	
	/**
	 * 获得url的路径，例如输入：http://www.abc.com/is/a/apple?id=3，返回/is/a/apple
	 * @param url
	 * @return
	 */
	public static String getUrlPath(String url) {
		if(url == null || url.isEmpty()) {
			return "";
		}
		try {
			URL _url = new URL(url);
			return _url.getPath();
		} catch (MalformedURLException e) {
			return "";
		}
	}
	
	/**
	 * 获得url的host，如果url不符合格式，会返回空字符串。
	 * 例如输入：http://www.abc.com/is/a/apple?id=3，返回www.abc.com，不带端口
	 * @param url
	 * @return
	 */
	public static String getUrlHostname(String url) {
		if(url == null || url.isEmpty()) {
			return "";
		}
		try {
			URL _url = new URL(url);
			return _url.getHost();
		} catch (MalformedURLException e) {
			return "";
		}
	}
	
	/**
	 * 获得url的host，如果url不符合格式，会返回空字符串。
	 * 例如输入：http://www.abc.com/is/a/apple?id=3，返回www.abc.com:80，一定会带上端口
	 * @param url
	 * @return
	 */
	public static String getUrlHostnameWithPort(String url) {
		if(url == null || url.isEmpty()) {
			return "";
		}
		try {
			URL _url = new URL(url);
			return _url.getHost() + ":" + _url.getPort();
		} catch (MalformedURLException e) {
			return "";
		}
	}
	
	/**
	 * 获得servlet应用的contextPath，即tomcat部署的应用名称的根目录，如admin.war部署之后返回 /admin
	 * @param request
	 * @return
	 */
	public static String getContextPath(HttpServletRequest request) {
		return request.getContextPath();
	}

}
