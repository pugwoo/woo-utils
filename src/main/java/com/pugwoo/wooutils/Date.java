package com.pugwoo.wooutils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 继承自java.util.Date，实现一些转换功能
 * 2011年9月19日 上午12:05:18 创建
 * 2011-12-15 23:07 新增写入、日期计算
 * 2016年4月29日 16:54:00 整理导入
 * 
 * 设计：提供String的输入输出格式，包括【自身输入输出格式】和【全局输入输出格式】
 * 
 */
public class Date extends java.util.Date {

	private static final long serialVersionUID = 1L;

	/**标准日期时间格式**/
	public final static String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/**MySQL日期时间格式**/
	public final static String MYSQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	/**中文日期格式**/
	public final static String CHINESE_DATE_FORMAT = "yyyy年MM月dd日";
	/**日期格式**/
	public final static String DATE_FORMAT = "yyyy-MM-dd";
	/**时间格式**/
	public final static String TIME_FORMAT = "HH:mm:ss";
	
	/**默认日期输入格式**/
	private String inputDateFormat = STANDARD_FORMAT;
	/**默认日期输出格式**/
	private String outputDateFormat = STANDARD_FORMAT;
	
	public Date() {
		super();
	}
	private Date(long time) {
		super(time);
	}
	
	public static Date valueOf(String dateTime) {
		try {
			return input(dateTime, null);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * @param dateTime
	 * @param pattern 当pattern不为null时，会设置到Date对象的默认输入和输出格式中
	 * @return
	 */
	public static Date valueOf(String dateTime, String pattern) {
		try {
			Date date = input(dateTime, pattern);
			date.inputDateFormat = pattern;
			date.outputDateFormat = pattern;
			return date;
		} catch (ParseException e) {
			return null;
		}
	}
		
	/**
	 * 优先级顺序：对象自身、全局、父类
	 */
	@Override
	public String toString() {
		return output(this, outputDateFormat);
	}
	
	/**
	 * 按照给定的格式输出String
	 * 
	 * @param pattern 格式
	 * @return
	 */
	public String format(String pattern) {
		return output(this, pattern);
	}
	
	/**
	 * @param field 对应于Calendar定义的域
	 */
	public void addTime(int field, int num) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.add(field, num);
		setTime(cal.getTimeInMillis());
	}
	
	/**
	 * @param dateTime
	 * @param pattern 如果pattern为null，则采用标准日期格式
	 * @return
	 * @throws ParseException 
	 */
	private static Date input(String dateTime, String pattern) throws ParseException {
		if (dateTime == null) {
			return null;
		}
		if (pattern == null) {
			pattern = STANDARD_FORMAT;
		}
		return new Date(new SimpleDateFormat(pattern).parse(dateTime).getTime());
	}
	
	/**
	 * @param date
	 * @param pattern 如果pattern为null，则采用标准日期格式
	 * @return
	 */
	private static String output(Date date, String pattern) {
		if (date == null) {
			return "";
		}
		if (pattern == null) {
			pattern = STANDARD_FORMAT;
		}
		return new SimpleDateFormat(pattern).format(date);
	}
	
	public String getOutputDateFormat() {
		return outputDateFormat;
	}

	public void setOutputDateFormat(String outputDateFormat) {
		this.outputDateFormat = outputDateFormat;
	}

	public String getInputDateFormat() {
		return inputDateFormat;
	}

	public void setInputDateFormat(String inputDateFormat) {
		this.inputDateFormat = inputDateFormat;
	}
}
