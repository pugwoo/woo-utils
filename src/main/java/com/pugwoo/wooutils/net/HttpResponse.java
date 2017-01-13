package com.pugwoo.wooutils.net;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 2016年2月4日 15:22:01 HTTP返回信息
 * 
 * @author pugwoo@gmail.com
 */
public class HttpResponse {

	/** HTTP状态码 */
	private int responseCode;

	/**
	 * HTTP头部
	 */
	private Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

	/**
	 * HTTP正文
	 */
	private byte[] contentBytes;

	/**
	 * 获得正文
	 * 
	 * @return
	 */
	public String getContentString() {
		// 尝试自动从http header里面获得编码
		List<String> contentType = headers.get("Content-Type");
		if(contentType != null && contentType.size() > 0) {
			String ctype = contentType.get(0);
			if(ctype != null) {
				int index = ctype.indexOf("charset=");
				if(index > 0) {
					String charset = ctype.substring(index + "charset=".length());
					if(charset != null && !charset.isEmpty()) {
						try {
							return new String(contentBytes, charset);
						} catch (UnsupportedEncodingException e) {
						}
					}
				}
			}
		}
		
		try {
			return new String(contentBytes, "utf-8"); // 默认 utf-8编码
		} catch (UnsupportedEncodingException e) {
			return new String(contentBytes);
		}
	}

	/**
	 * 获得正文
	 * 
	 * @param charset
	 * @return
	 */
	public String getContentString(String charset) {
		try {
			return new String(contentBytes, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getResponseCode() {
		return responseCode;
	}

	public byte[] getContentBytes() {
		return contentBytes;
	}

	public void setContentBytes(byte[] contentBytes) {
		this.contentBytes = contentBytes;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

}
