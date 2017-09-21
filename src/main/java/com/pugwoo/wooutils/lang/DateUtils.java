package com.pugwoo.wooutils.lang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

	// 常用格式 //
	
	/**标准日期时间格式**/
	public final static String FORMAT_STANDARD = "yyyy-MM-dd HH:mm:ss";
	/**MySQL日期时间格式**/
	public final static String FORMAT_MYSQL_DATETIME = "yyyy-MM-dd HH:mm:ss.SSS";
	/**中文日期格式**/
	public final static String FORMAT_CHINESE_DATE = "yyyy年MM月dd日";
	/**日期格式**/
	public final static String FORMAT_DATE = "yyyy-MM-dd";
	/**时间格式**/
	public final static String FORMAT_TIME = "HH:mm:ss";
	
	@SuppressWarnings("serial")
	public static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
	    put("^\\d{8}$", "yyyyMMdd"); // 20170306
	    put("^\\d{14}$", "yyyyMMddHHmmss"); // 20170306152356
	    put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss"); // 20170306 152356
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd"); // 2017-03-06
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd"); // 2017/03/06
	    put("^\\d{1,2}:\\d{1,2}:\\d{1,2}$", "HH:mm:ss"); // 16:34:32
	    put("^\\d{1,2}:\\d{1,2}$", "HH:mm"); // 16:34
	    put("^\\d{4}年\\d{1,2}月\\d{1,2}日$", "yyyy年MM月dd日"); // 2017年3月30日
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm"); // 2017-03-06 15:23
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm"); // 2017/03/06 15:23
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm:ss"); // 2017-03-06 15:23:56
	    put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm:ss"); // 2017/03/06 15:23:56
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd HH:mm:ss.SSS"); // 2017-10-18 16:00:00.000
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}Z$", "yyyy-MM-dd'T'HH:mm:ss.SSSX"); // 2017-10-18T16:00:00.000Z
	    put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd'T'HH:mm:ss.SSS"); // 2017-10-18T16:00:00.000
	}};
	
	/**
	 * @param field 对应于Calendar定义的域
	 */
	public static Date addTime(Date date, int field, int num) {
		if(date == null) {return null;}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(field, num);
		return cal.getTime();
	}
	
	/**
	 * 自动解析
	 * @param date
	 * @return
	 */
	public static Date parse(String date) throws ParseException {
		if(date == null || date.trim().isEmpty()) {
			return null;
		}
		String format = determineDateFormat(date);
		if(format == null) {
			throw new ParseException("Unparseable date: \"" + date +
				"\". Supported formats: " + DATE_FORMAT_REGEXPS.values(), -1);
		}
		return new SimpleDateFormat(format).parse(date);
	}
	
	/**
	 * 解析日期
	 * @param date
	 * @param pattern 日期格式pattern
	 */
	public static Date parse(String date, String pattern) throws ParseException {
		if(date == null || date.trim().isEmpty()) {
			return null;
		}
		return new SimpleDateFormat(pattern).parse(date);
	}
	
	/**
	 * 自动解析，不返回异常，当出现异常时返回null
	 * @param date
	 * @return
	 */
	public static Date parseSwallowException(String date) {
		try {
			return parse(date);
		} catch (ParseException e) {
			LOGGER.error("parse fail:{}", date, e);
			return null;
		}
	}
	
	/**
	 * 解析日期，不返回异常，当出现异常时返回null
	 * @param date
	 * @return
	 */
	public static Date parseSwallowException(String date, String pattern) {
		try {
			return parse(date, pattern);
		} catch (ParseException e) {
			LOGGER.error("parse fail:{}", date, e);
			return null;
		}
	}
	
	/**
	 * 转换成标准的格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String format(Date date) {
		if(date == null) {
			return "";
		}
		return new SimpleDateFormat(FORMAT_STANDARD).format(date);
	}
	
	/**
	 * 转换成标准的格式 yyyy-MM-dd
	 */
	public static String formatDate(Date date) {
		if(date == null) {
			return "";
		}
		return new SimpleDateFormat(FORMAT_DATE).format(date);
	}
	
	public static String format(Date date, String pattern) {
		if(date == null) {
			return "";
		}
		return new SimpleDateFormat(pattern).format(date);
	}
	
	private static String determineDateFormat(String dateString) {
	    for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
	        if (dateString.matches(regexp)) {
	            return DATE_FORMAT_REGEXPS.get(regexp);
	        }
	    }
	    return null; // Unknown format.
	}
	
}
