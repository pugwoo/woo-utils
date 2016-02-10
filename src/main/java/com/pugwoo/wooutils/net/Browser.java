package com.pugwoo.wooutils.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 2016年2月4日 15:16:42 
 * 模拟一个浏览器发HTTP请求，不包括页面处理
 * 
 * 计划支持的特性：
 * 1. 支持指定为输出流
 * 2. 支持cookie
 * 
 * 
 * @author pugwoo@gmail.com
 */
public class Browser {

	/** 默认浏览器userAgent:Chrome Win7*/
	private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36";

	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl) throws IOException {
		return post(httpUrl, new HashMap<String, String>());
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, Map<String, String> params) throws IOException {
		return post(httpUrl, buildPostString(params));
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, byte[] postData) throws IOException {
		return post(httpUrl, new ByteArrayInputStream(postData));
	}
	
	/**
	 * post方式请求HTTP
	 * @param httpUrl
	 * @return
	 * @throws IOException
	 */
	public HttpResponse post(String httpUrl, InputStream in) throws IOException {
		HttpURLConnection urlConnection = getUrlConnection(httpUrl, "POST");
		
		// POST 数据
		if(in != null) {
			urlConnection.setDoOutput(true);
	        OutputStream os = urlConnection.getOutputStream();
	        byte[] buf = new byte[4096];
	        int readBytes = 0;
	        while((readBytes = in.read(buf)) != -1) {
	        	os.write(buf, 0, readBytes);
	        }
	        os.flush();
	        os.close();
		}
        
		HttpResponse httpResponse = new HttpResponse();
		makeHttpResponse(urlConnection, httpResponse);
		return httpResponse;
	}
	
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
		httpUrl = appendParamToUrl(httpUrl, params);
		
		HttpURLConnection urlConnection = getUrlConnection(httpUrl, "GET");
		
		// 301 302 跳转处理
		int responseCode = urlConnection.getResponseCode();
		if(responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
			List<String> location = urlConnection.getHeaderFields().get("Location");
			if(location != null && !location.isEmpty()) {
				if(!httpUrl.equals(location.get(0))) {
					return get(location.get(0), params);
				}
			}
		}

		HttpResponse httpResponse = new HttpResponse();
		makeHttpResponse(urlConnection, httpResponse);
		return httpResponse;
	}
	
	/////////////////// 以下是工具方法 ////////////////////////////
	
	/**
	 * 拿到http连接对象
	 * @param httpUrl
	 * @param method
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection getUrlConnection(String httpUrl, String method) throws IOException {
		URL url = new URL(httpUrl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod(method);
		urlConnection.setRequestProperty("User-agent", USER_AGENT);
		
		return urlConnection;
	}
	
	/**
	 * 构造httpResponse
	 * @param urlConnection
	 * @param httpResponse
	 * @throws IOException 
	 */
	private void makeHttpResponse(HttpURLConnection urlConnection, HttpResponse httpResponse)
			throws IOException {
		httpResponse.setResponseCode(urlConnection.getResponseCode());
		httpResponse.setHeaders(urlConnection.getHeaderFields());
		
		InputStream in = urlConnection.getInputStream();
		byte[] buf = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len;
		while((len = in.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		httpResponse.setContentBytes(baos.toByteArray());
	}
	
	private static byte[] buildPostString(Map<String, String> params) {
		if(params == null || params.isEmpty()) {
			try {
				return "".getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				return new byte[0];
			}
		}
		StringBuilder sb = new StringBuilder();
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
		
		try {
			return sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new byte[0];
		}
	}
	
	/**
	 * 将请求参数加到httpUrl后面
	 * @param httpUrl
	 * @param params
	 * @return
	 */
	private static String appendParamToUrl(String httpUrl, Map<String, String> params) {
		if(params == null || params == null || params.isEmpty()) {
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
