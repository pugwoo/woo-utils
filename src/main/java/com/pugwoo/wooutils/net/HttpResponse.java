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
	public String getContetString() {
		return new String(contentBytes);
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
