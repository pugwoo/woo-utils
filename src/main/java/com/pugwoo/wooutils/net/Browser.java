package com.pugwoo.wooutils.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 2016年2月4日 15:16:42 
 * 模拟一个浏览器发HTTP请求，不包括页面处理
 * 
 * @author pugwoo@gmail.com
 */
public class Browser {

	/** 默认浏览器userAgent:Chrome Win7*/
	private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36";

	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse get(String httpUrl) throws IOException {
		return get(httpUrl, null);
	}
	
	/**
	 * get方式请求HTTP,处理细节：<br>
	 * 1. 如果是301或302跳转，且跳转链接不同于当前链接，则再请求跳转的URL
	 * 
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse get(String httpUrl, Map<String, String> params) throws IOException {
		HttpResponse httpResponse = new HttpResponse();
		httpUrl = appendParamToUrl(httpUrl, params);
		
		URL url = new URL(httpUrl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setRequestProperty("User-agent", USER_AGENT);
		
		int responseCode = urlConnection.getResponseCode();
		httpResponse.setResponseCode(responseCode);
		httpResponse.setHeaders(urlConnection.getHeaderFields());
		
		// 301 302 处理
		if(responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
			List<String> location = httpResponse.getHeaders().get("Location");
			if(location != null && !location.isEmpty()) {
				if(!httpUrl.equals(location.get(0))) {
					return get(location.get(0), params);
				}
			}
		}
		
		InputStream in = urlConnection.getInputStream();
		byte[] buf = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len;
		while((len = in.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		httpResponse.setContentBytes(baos.toByteArray());

		return httpResponse;
	}
	
	/////////////////// 以下是工具方法 ////////////////////////////
	
	/**
	 * 将请求参数加到httpUrl后面
	 * @param httpUrl
	 * @param params
	 * @return
	 */
	private static String appendParamToUrl(String httpUrl, Map<String, String> params) {
		if(params == null || params.isEmpty()) {
			return httpUrl;
		}
		
		StringBuilder sb = new StringBuilder(httpUrl);
		int indexOfQuestion = httpUrl.indexOf("?");
		if(indexOfQuestion == -1) {
			sb.append("?");
		} else {
			if(indexOfQuestion < httpUrl.length() - 1 && !httpUrl.endsWith("&")) {
				sb.append("&");
			}
		}
		
		boolean needAppendAnd = false;
		for(Entry<String, String> entry : params.entrySet()) {
			try {
				if(needAppendAnd) {
					sb.append("&");
				}
				sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				sb.append("=");
				sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				needAppendAnd = true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
}
