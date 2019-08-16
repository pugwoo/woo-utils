package com.pugwoo.wooutils.net;

import com.pugwoo.wooutils.string.StringTools;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class NetUtils {

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
	 * @return 可能返回多个ip，以逗号分隔
	 */
	public static String getRemoteIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if(StringTools.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if(StringTools.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(StringTools.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if(StringTools.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if(StringTools.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
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
