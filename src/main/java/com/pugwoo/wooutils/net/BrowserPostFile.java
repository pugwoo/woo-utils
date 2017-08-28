package com.pugwoo.wooutils.net;

import java.io.InputStream;

/**
 * post文件
 */
public class BrowserPostFile {
	
	// 文件名称
	private String filename;

	/**
	 * 文件contentType，常用的contentType:
	 * 1. text/plain 文本
	 * 2. image/png 图片
	 * 3. image/jpeg 图片
	 * 4. application/octet-stream    二进制流，例如zip文件
	 */
	private String contentType;
	
	// 输入流in（in和bytes二选一）
	private InputStream in;
	
	// 输入字节bytes（in和bytes二选一）
	private byte[] bytes;
	
	public BrowserPostFile(String filename, String contentType, InputStream in) {
		this.filename = filename;
		this.contentType = contentType;
		this.in = in;
	}
	
	public BrowserPostFile(String filename, String contentType, byte[] bytes) {
		this.filename = filename;
		this.contentType = contentType;
		this.bytes = bytes;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public InputStream getIn() {
		return in;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
}
