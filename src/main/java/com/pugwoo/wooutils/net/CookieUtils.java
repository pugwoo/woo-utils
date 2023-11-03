package com.pugwoo.wooutils.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * cookie相关功能，cookie默认都用URLEncode编码和解码。
 * 1. 支持指定domain，所有path都设置为根目录，不支持子path的cookie，也不建议这样用，因为path很不稳定。
 *    也建议指定domain，可以是子域名，也可以是根域名，必须是同域名
 * 2. 删除cookie请使用removeCookie
 * @author pugwoo
 */
public class CookieUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CookieUtils.class);
	
	/**
	 * 根据name读取cookie的值
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getCookieValue(HttpServletRequest request, String name) {
		if(name == null) {
			return null;
		}
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				if(name.equals(cookie.getName())) {
					try {
						return URLDecoder.decode(cookie.getValue(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						LOGGER.error("URLEncoder.decode fail, value:{}", cookie.getValue(), e);
						return cookie.getValue();
					}
				}
			}
		}
		return null;
	}

	/**
	 * 根据name读取cookie的值
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getCookieValueForJakarta(jakarta.servlet.http.HttpServletRequest request, String name) {
		if(name == null) {
			return null;
		}
		jakarta.servlet.http.Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (jakarta.servlet.http.Cookie cookie : cookies) {
				if(name.equals(cookie.getName())) {
					try {
						return URLDecoder.decode(cookie.getValue(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						LOGGER.error("URLEncoder.decode fail, value:{}", cookie.getValue(), e);
						return cookie.getValue();
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据name读取cookie的值，支持多个相同cookie name的情况
	 * @param request
	 * @param name
	 * @return
	 */
	public static List<String> getCookieValues(HttpServletRequest request, String name) {
		if(name == null) {
			return null;
		}
		List<String> cookieList = new ArrayList<String>();
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				if(name.equals(cookie.getName())) {
					try {
						cookieList.add(URLDecoder.decode(cookie.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						LOGGER.error("URLEncoder.decode fail, value:{}", cookie.getValue(), e);
						cookieList.add(cookie.getValue());
					}
				}
			}
		}
		return cookieList;
	}

	/**
	 * 根据name读取cookie的值，支持多个相同cookie name的情况
	 * @param request
	 * @param name
	 * @return
	 */
	public static List<String> getCookieValuesForJakarta(jakarta.servlet.http.HttpServletRequest request, String name) {
		if(name == null) {
			return null;
		}
		List<String> cookieList = new ArrayList<String>();
		jakarta.servlet.http.Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (jakarta.servlet.http.Cookie cookie : cookies) {
				if(name.equals(cookie.getName())) {
					try {
						cookieList.add(URLDecoder.decode(cookie.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						LOGGER.error("URLEncoder.decode fail, value:{}", cookie.getValue(), e);
						cookieList.add(cookie.getValue());
					}
				}
			}
		}
		return cookieList;
	}
	
	/**
	 * 增加cookie，如果name相同的话，浏览器会把cookie值追加要已有的之上
	 * 
	 * @param response
	 * @param name cookie名字
	 * @param value cookie值，不建议为null值
	 * @param domain 指定域名，null表示不指定
	 * @param expireSeconds cookie生命周期 以秒为单位，当设置为0时，cookie默认有效期10年；如果删除，请用removeCookie方法
	 */
	public static void addCookie(HttpServletResponse response, String name, String value,
			String domain, int expireSeconds) {
		try {
			value = value == null ? null : URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URLEncoder.encode fail, value:{}", value, e);
		}
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		if(domain != null) {
			cookie.setDomain(domain);
		}
		if (expireSeconds == 0) {
			cookie.setMaxAge(10 * 365 * 24 * 3600);
		} else {
			cookie.setMaxAge(expireSeconds);
		}
		response.addCookie(cookie);
	}

	/**
	 * 增加cookie，如果name相同的话，浏览器会把cookie值追加要已有的之上
	 *
	 * @param response
	 * @param name cookie名字
	 * @param value cookie值，不建议为null值
	 * @param domain 指定域名，null表示不指定
	 * @param expireSeconds cookie生命周期 以秒为单位，当设置为0时，cookie默认有效期10年；如果删除，请用removeCookie方法
	 */
	public static void addCookieForJakarta(jakarta.servlet.http.HttpServletResponse response, String name, String value,
								 String domain, int expireSeconds) {
		try {
			value = value == null ? null : URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("URLEncoder.encode fail, value:{}", value, e);
		}
		jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, value);
		cookie.setPath("/");
		if(domain != null) {
			cookie.setDomain(domain);
		}
		if (expireSeconds == 0) {
			cookie.setMaxAge(10 * 365 * 24 * 3600);
		} else {
			cookie.setMaxAge(expireSeconds);
		}
		response.addCookie(cookie);
	}
	
	/**
	 * 增加cookie，如果name相同的话，浏览器会把cookie值追加要已有的之上。有效期100年。
	 * 
	 * @param response
	 * @param name
	 * @param value
	 * @param domain
	 */
	public static void addCookie(HttpServletResponse response, String name, String value,
			String domain) {
		addCookie(response, name, value, domain, 0);
	}

	/**
	 * 增加cookie，如果name相同的话，浏览器会把cookie值追加要已有的之上。有效期100年。
	 *
	 * @param response
	 * @param name
	 * @param value
	 * @param domain
	 */
	public static void addCookieForJakarta(jakarta.servlet.http.HttpServletResponse response, String name, String value,
								 String domain) {
		addCookieForJakarta(response, name, value, domain, 0);
	}

	/**
	 * 删除cookie
	 * @param response
	 * @param name
	 * @param domain 当为null时表示不指定
	 */
	public static void removeCookie(HttpServletResponse response, String name, String domain) {
		Cookie cookie = new Cookie(name, "");
		cookie.setPath("/");
		if(domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setMaxAge(0); // delete
		response.addCookie(cookie);
	}

	/**
	 * 删除cookie
	 * @param response
	 * @param name
	 * @param domain 当为null时表示不指定
	 */
	public static void removeCookieForJakarta(jakarta.servlet.http.HttpServletResponse response, String name, String domain) {
		jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, "");
		cookie.setPath("/");
		if(domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setMaxAge(0); // delete
		response.addCookie(cookie);
	}
	
}
