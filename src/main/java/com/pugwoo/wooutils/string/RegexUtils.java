package com.pugwoo.wooutils.string;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式相关工具
 * @author nick
 */
public class RegexUtils {

	/**
	 * 获得第一个匹配到的字符串，推荐使用group regex，
	 * 将返回所有group的值的拼凑，推荐只用一个group，没有匹配到返回null。
	 * @param str
	 * @param regex
	 * @return
	 */
	public static String getFirstMatchStr(String str, String regex) {
		Pattern p = Pattern.compile(regex);
	    Matcher m = p.matcher(str);
	    while (m.find() == true) {
	    	int groupCount = m.groupCount();
	    	if(groupCount == 0) {
	    		return m.group();
	    	} else if (groupCount == 1) {
	    		return m.group(1);
	    	} else {
	    		StringBuilder sb = new StringBuilder();
	    		for(int i = 1; i <= groupCount; i++) {
	    			sb.append(m.group(i));
	    		}
	    		return sb.toString();
	    	}
	    }
	    return null;
	}
	
	/**
	 * 获得所有匹配到的字符串，推荐使用group regex，
	 * 将返回所有group的值的拼凑，推荐只用一个group，没有匹配到返回empty list。
	 * @param str
	 * @param regex
	 * @return
	 */
	public static List<String> getAllMatchStr(String str, String regex) {
		Pattern p = Pattern.compile(regex);
	    Matcher m = p.matcher(str);
	    List<String> result = new ArrayList<String>();
	    while (m.find() == true) {
	    	int groupCount = m.groupCount();
	    	if(groupCount == 0) {
	    		result.add(m.group());
	    	} else if (groupCount == 1) {
	    		result.add(m.group(1));
	    	} else {
	    		StringBuilder sb = new StringBuilder();
	    		for(int i = 1; i <= groupCount; i++) {
	    			sb.append(m.group(i));
	    		}
	    		result.add(sb.toString());
	    	}
	    }
	    return result;
	}
	
}
