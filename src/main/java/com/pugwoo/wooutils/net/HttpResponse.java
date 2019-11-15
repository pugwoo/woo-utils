package com.pugwoo.wooutils.net;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pugwoo.wooutils.string.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpResponse.class);

	/**请求的编码，可以由使用方自行指定，将在getContentString()方法中使用*/
	private String charset;

	/** HTTP状态码 */
	private int responseCode;

	/**
	 * HTTP头部
	 */
	private Map<String, List<String>> headers = new LinkedHashMap<>();

	/**
	 * HTTP正文
	 */
	@JsonIgnore
	private byte[] contentBytes;
	
	/**
	 * 异步下载的future
	 */
	@JsonIgnore
	private Browser.HttpResponseFuture future;
	
	/**
	 * 获得文件长度，-1表示未知
	 * @return
	 */
	public long getContentLength() {
		List<String> cl = headers.get("Content-Length");
		if(cl == null || cl.isEmpty()) {
			return -1;
		}
		try {
			return new Long(cl.get(0));
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * 获得已下载的字节数，非异步下载返回-1
	 * @return
	 */
	public long getDownloadedBytes() {
		if(future == null) {
			return -1;
		}
		return future.downloadedBytes;
	}
	
	/**
	 * 获取下载是否已经完成，针对异步下载而言
	 * @return
	 */
	@JsonIgnore
	public boolean isDownloadFinished() {
		if(future == null) {
			return true;
		}
		return future.isFinished;
	}

	/**
	 * 获得正文，会从头部自动识别编码（如果头部有Content-Type）。
	 * 但是没有，但是html中才有meta charset，那么不处理，请手工指定。
	 * 
	 * @return
	 */
	public String getContentString() {
		if(charset == null) {
			// 尝试自动从http header里面获得编码
			List<String> contentType = headers.get("Content-Type");
			if(contentType != null && contentType.size() > 0) {
				String ctype = contentType.get(0);
				if(ctype != null) {
					int index = ctype.indexOf("charset=");
					if(index > 0) {
						String cs = ctype.substring(index + "charset=".length());
						if(StringTools.isNotBlank(cs)) {
							charset = cs;
						}
					}
				}
			}
		}

		if(charset == null) {
			charset = "utf-8"; // 默认 utf-8编码
		}

		try {
			return new String(contentBytes, charset);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("convert byte[] to string error, charset:{}", charset, e);
			return new String(contentBytes);
		}
	}

	/**
	 * 获得正文,指定编码方式
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

	public void setFuture(Browser.HttpResponseFuture future) {
		this.future = future;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
}
