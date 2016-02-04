package com.pugwoo.wooutils;

/**
 * 2016年2月4日 11:29:00
 * 字符串工具类
 */
public class StringTools {

	/**
	 * 判定字符串是否只包含英文字母(不区分大小写)或数字
	 * @param str 不能为null
	 * @return 空字符串时返回true
	 */
    public static boolean isEnglishLetterOrNumeric(String str) {
    	int length = str.length();
    	for(int i = 0; i < length; i++) {
    		char c = str.charAt(i);
    		if(!(c>='a' && c<='z' || c>='A' && c<='Z' || c>='0' && c<='9')) {
    			return false;
    		}
    	}
    	return true;
    }
	
}
