package com.pugwoo.wooutils.string;

import java.io.UnsupportedEncodingException;

public class Base64 {

	/**
	 * 使用系统默认编码，现在默认的多是utf-8编码
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		return java.util.Base64.getEncoder().encodeToString(str.getBytes());
	}
	
	public static String encode(byte[] bytes) {
		return java.util.Base64.getEncoder().encodeToString(bytes);
	}
	
	public static byte[] decode(String str) {
		return java.util.Base64.getDecoder().decode(str);
	}
	
	/**
	 * 将UnsupportedEncodingException包成RuntimeException
	 * @param str
	 * @param charset
	 * @return
	 */
	public static String decode(String str, String charset) {
		try {
			return new String(decode(str), charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
