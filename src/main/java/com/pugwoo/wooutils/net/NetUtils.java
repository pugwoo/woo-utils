package com.pugwoo.wooutils.net;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

public class NetUtils {

	/**
	 * 获得客户端的ip地址
	 * @return
	 */
	public static String getRemoteIp(HttpServletRequest request) {
		String remoteIp = request.getHeader("X-Forwarded-For");
		if(remoteIp != null && !remoteIp.trim().isEmpty()) {
			return remoteIp;
		}
		return request.getRemoteAddr();
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
				+ ("http".equals(request.getScheme()) && request.getServerPort() == 80
						|| "https".equals(request.getScheme()) && request.getServerPort() == 443 ? ""
								: ":" + request.getServerPort());
	}
	
	/**
	 * 获得当前域名，不带端口，例如www.abc.com
	 * @param request
	 * @return
	 */
	public static String getHostname(HttpServletRequest request) {
		String serverName = request.getServerName();
		return serverName;
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
	 * 获得url的路径，例如输入：http://www.abc.com/is/a/apple?id=3，返回/is/a/apple
	 * @param url
	 * @return
	 */
	public static String getPathFromUrl(String url) {
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
	
}
