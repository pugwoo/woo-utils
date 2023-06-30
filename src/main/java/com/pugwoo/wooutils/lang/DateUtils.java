package com.pugwoo.wooutils.lang;

import com.pugwoo.wooutils.string.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 特别说明：Date是有时区的，默认使用操作系统的时区
 */
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

	public static final Map<String, String> DATE_FORMAT_REGEXPS = new LinkedHashMap<String, String>() {{

		// 最常用的
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm:ss"); // 2017-03-06 15:23:56
		put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd"); // 2017-03-06

		put("^\\d{6}$", "yyyyMM"); // 201703
		put("^\\d{8}$", "yyyyMMdd"); // 20170306
		put("^\\d{14}$", "yyyyMMddHHmmss"); // 20170306152356
		put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss"); // 20170306 152356
		put("^\\d{4}-\\d{1,2}$", "yyyy-MM"); // 2017-03
		put("^\\d{4}/\\d{1,2}$", "yyyy/MM"); // 2017/03

		put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd"); // 2017/03/06
		put("^\\d{1,2}:\\d{1,2}:\\d{1,2}$", "HH:mm:ss"); // 16:34:32
		put("^\\d{1,2}:\\d{1,2}$", "HH:mm"); // 16:34
		put("^\\d{4}年\\d{1,2}月\\d{1,2}日$", "yyyy年MM月dd日"); // 2017年3月30日
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd HH:mm"); // 2017-03-06 15:23
		put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm"); // 2017/03/06 15:23

		put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy/MM/dd HH:mm:ss"); // 2017/03/06 15:23:56
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd HH:mm:ss.SSS"); // 2017-10-18 16:00:00.000
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}Z$", "yyyy-MM-dd'T'HH:mm:ss.SSSX"); // 2017-10-18T16:00:00.000Z
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}$", "yyyy-MM-dd'T'HH:mm:ss.SSS"); // 2017-10-18T16:00:00.000
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}$", "yyyy-MM-dd'T'HH:mm:ss"); // 2017-10-18T16:00:00
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}[+-]{1}\\d{4}$", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // 2017-10-18T16:00:00.000+0000
		put("^\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3} [+-]{1}\\d{4}$", "yyyy-MM-dd'T'HH:mm:ss.SSS Z"); // 2017-10-18T16:00:00.000 +0000
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
	 * 自动解析各种格式的日期，不会抛出异常。<br>
	 * <br>
	 * 说明：<br>
	 * 会尝试将数字当做时间戳转换，但只转换值大于等于946656000的时间戳，即2000年以后的 <br>
	 *
	 * @return 解析失败返回null，同时log error
	 */
	public static Date parse(String date) {
		if(date == null) {
			return null;
		}
		date = date.trim();
		if(date.isEmpty()) {
			return null;
		}
		String pattern = determineDateFormat(date);
		if(pattern == null) {
			// 检查是否是时间戳
			Date date2 = tryParseTimestamp(date);
			if(date2 != null) {
				return date2;
			}

			LOGGER.error("date parse, pattern not support, date:{}", date);
			return null;
		}
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			simpleDateFormat.setLenient(false);
			return simpleDateFormat.parse(date);
		} catch (ParseException e) {
			if ("0000-00-00 00:00:00".equals(date) || "0000-00-00".equals(date)) {
				return null;
			}
			LOGGER.error("parse date error, date:{}", date, e);
			return null;
		}
	}

	private static Date tryParseTimestamp(String date) {
		if (!StringTools.isDigit(date)) {
			return null;
		}
		Long timestamp = NumberUtils.parseLong(date);
		if (timestamp == null) {
			return null;
		}

		if (timestamp < 946656000) { // 不处理2000-01-01以前的时间戳
			return null;
		}

		// 时间戳小于42亿则认为是秒（此时已经是2103-02-04），否则是毫秒
		if(timestamp < 4200000000L) {
			return new Date(timestamp * 1000L);
		} else {
			return new Date(timestamp);
		}
	}

	/**失败返回null，不会抛异常*/
	public static LocalDateTime parseLocalDateTime(String date) {
		return toLocalDateTime(parse(date));
	}

	public static LocalDateTime toLocalDateTime(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**失败返回null，不会抛异常*/
	public static LocalDate parseLocalDate(String date) {
		return toLocalDate(parse(date));
	}

	public static LocalDate toLocalDate(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**失败返回null，不会抛异常*/
	public static LocalTime parseLocalTime(String date) {
		return toLocalTime(parse(date));
	}

	public static LocalTime toLocalTime(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
	}

	/**
	 * 解析日期，不会抛出异常，如果解析失败，打log并返回null
	 * @param pattern 日期格式pattern
	 */
	public static Date parse(String date, String pattern) {
		if(date == null || date.trim().isEmpty()) {
			return null;
		}
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			simpleDateFormat.setLenient(false);
			return simpleDateFormat.parse(date);
		} catch (ParseException e) {
			if ("0000-00-00 00:00:00".equals(date) || "0000-00-00".equals(date)) {
				return null;
			}
			LOGGER.error("parse date error, date:{}", date, e);
			return null;
		}
	}

	/**
	 * 自动解析，失败抛出异常<br>
	 *
	 * 说明：<br>
	 * 会尝试将数字当做时间戳转换，但只转换值大于等于946656000的时间戳，即2000年以后的 <br>
	 *
	 */
	public static Date parseThrowException(String date) throws ParseException {
		if(date == null) {
			return null;
		}
		date = date.trim();
		if(date.isEmpty()) {
			return null;
		}

		String pattern = determineDateFormat(date);
		if(pattern == null) {
			// 检查是否是时间戳
			Date _date = tryParseTimestamp(date);
			if(_date != null) {
				return _date;
			}

			throw new ParseException("Unparseable date: \"" + date +
					"\". Supported formats: " + DATE_FORMAT_REGEXPS.values(), -1);
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setLenient(false);
		try {
			return simpleDateFormat.parse(date);
		} catch (Exception e) {
			if ("0000-00-00 00:00:00".equals(date) || "0000-00-00".equals(date)) {
				return null;
			}
			throw e;
		}
	}

	/**
	 * 解析日期，失败抛出异常
	 */
	public static Date parseThrowException(String date, String pattern) throws ParseException {
		if(date == null || date.trim().isEmpty()) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setLenient(false);
		try {
			return simpleDateFormat.parse(date);
		} catch (Exception e) {
			if ("0000-00-00 00:00:00".equals(date) || "0000-00-00".equals(date)) {
				return null;
			}
			throw e;
		}
	}

	/**
	 * 转换成标准的格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String format(Date date) {
		if(date == null) {
			return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_STANDARD);
		simpleDateFormat.setLenient(false);
		return simpleDateFormat.format(date);
	}

	/**
	 * 转换成标准的格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String format(LocalDateTime localDateTime) {
		if(localDateTime == null) {
			return "";
		}
		return localDateTime.format(DateTimeFormatter.ofPattern(DateUtils.FORMAT_STANDARD));
	}

	/**
	 * 转换成标准的格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String format(LocalDate localDate) {
		if(localDate == null) {
			return "";
		}
		return localDate.format(DateTimeFormatter.ofPattern(DateUtils.FORMAT_DATE));
	}

	/**
	 * 转换成标准的格式 yyyy-MM-dd
	 */
	public static String formatDate(Date date) {
		if(date == null) {
			return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE);
		simpleDateFormat.setLenient(false);
		return simpleDateFormat.format(date);
	}

	/**
	 * 转换成标准的格式 yyyy-MM-dd
	 */
	public static String formatDate(LocalDateTime localDateTime) {
		if(localDateTime == null) {
			return "";
		}
		return localDateTime.format(DateTimeFormatter.ofPattern(DateUtils.FORMAT_DATE));
	}

	/**
	 * 转换成标准的格式 yyyy-MM-dd
	 */
	public static String formatDate(LocalDate localDate) {
		if(localDate == null) {
			return "";
		}
		return localDate.format(DateTimeFormatter.ofPattern(DateUtils.FORMAT_DATE));
	}

	public static String format(Date date, String pattern) {
		if(date == null) {
			return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setLenient(false);
		return simpleDateFormat.format(date);
	}

	public static String format(LocalDateTime date, String pattern) {
		if(date == null) {
			return "";
		}
		return date.format(DateTimeFormatter.ofPattern(pattern));
	}

	public static String format(LocalDate date, String pattern) {
		if(date == null) {
			return "";
		}
		return date.format(DateTimeFormatter.ofPattern(pattern));
	}

	public static String format(LocalTime date, String pattern) {
		if(date == null) {
			return "";
		}
		return date.format(DateTimeFormatter.ofPattern(pattern));
	}

	private static String determineDateFormat(String dateString) {
		for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
			if (dateString.matches(regexp)) {
				return DATE_FORMAT_REGEXPS.get(regexp);
			}
		}
		return null; // Unknown format.
	}

	// ======================================

	public static int getYear(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	public static int getYear(LocalDate localDate) {
		if (localDate == null) {
			return 0;
		}
		return localDate.getYear();
	}

	/**
	 * 获得日期的月份值，从1开始，1到12
	 */
	public static int getMonth(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MONTH) + 1;
	}


	/**
	 * 获得日期的月份值，从1开始，1到12
	 */
	public static int getMonth(LocalDate localDate) {
		if (localDate == null) {
			return 0;
		}
		return localDate.getMonthValue();
	}

	public static int getDay(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DATE);
	}

	public static int getDay(LocalDate localDate) {
		if (localDate == null) {
			return 0;
		}
		return localDate.getDayOfMonth();
	}

	/**
	 * 24小时制
	 */
	public static int getHour(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public static int getMinute(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MINUTE);
	}

	public static int getSecond(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.SECOND);
	}

	public static int getMilliSecond(Date date) {
		if (date == null) {
			return 0;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MILLISECOND);
	}

	// ======================================

	// 当前系统时区距离0时区的毫秒数，例如东8区是28800000
	private static final int timezoneOffset = Calendar.getInstance().get(Calendar.ZONE_OFFSET);

	/**
	 * 获得date到当前时间间隔了多少天，如果date是今天则返回0，如果date是昨天则返回1，以此类推。
	 * 如果是未来的日期，则返回-1、-2以此类推。
	 * 【该接口性能最佳】
	 */
	public static long getDaysToToday(Date date) {
		long now = System.currentTimeMillis();
		long today = now - (now % (24 * 3600 * 1000)) - timezoneOffset;
		if(now - today >= (24 * 3600 * 1000)) {
			today += (24 * 3600 * 1000);
		}
		long dateTimestamp = date.getTime();
		if(dateTimestamp < today) {
			return (today - dateTimestamp) / (24 * 3600 * 1000) + 1;
		} else {
			return -((dateTimestamp - today) / (24 * 3600 * 1000));
		}
	}

	/**
	 * 获得date所在月份的第一天的日期
	 */
	public static LocalDate getFirstDayOfMonth(Date date) {
		if (date == null) {
			return null;
		}
		LocalDate localDate = toLocalDate(date);
		return localDate.withDayOfMonth(1);
	}

	/**
	 * 获得date所在的月份的最后一天的日期
	 */
	public static LocalDate getLastDayOfMonth(Date date) {
		if (date == null) {
			return null;
		}
		LocalDate localDate = toLocalDate(date);
		return localDate.withDayOfMonth(1).plusMonths(1).minusDays(1);
	}

	/**
	 * 获得指定时间date的当天的开始时间。
	 * 例如date为2019-01-02 03:04:05时，返回2019-01-02 00:00:00
	 * 【该接口性能最佳】
	 */
	public static Date getStartTimeOfDay(Date date) {
		long now = date.getTime();
		long today = now - (now % (24 * 3600 * 1000)) - timezoneOffset;
		if(now - today >= (24 * 3600 * 1000)) {
			today += (24 * 3600 * 1000);
		}
		return new Date(today);
	}

	/**
	 * 获得指定时间date的当天的结束时间。
	 * 例如date为2019-01-02 03:04:05时，返回2019-01-02 23:59:59.999
	 * 【该接口性能最佳】
	 */
    public static Date getEndTimeOfDay(Date date) {
		long now = date.getTime();
		long today = now - (now % (24 * 3600 * 1000)) - timezoneOffset;
		if(now - today >= (24 * 3600 * 1000)) {
			today += (24 * 3600 * 1000);
		}
		today += (24 * 3600 * 1000) - 1;
		return new Date(today);
	}

	/**
	 * 计算两个日期的天数差，不足一天的不算。
	 * @return 返回值都大于等于0，不关心date1和date2的顺序
	 */
	public static int diffDays(Date date1, Date date2) {
		if(date1 == null || date2 == null) {
			return 0;
		}
		return (int) (Math.abs(date1.getTime() - date2.getTime()) / (24 * 3600 * 1000));
	}

	/**
	 * 计算两个日期的年份差，主要用于计算年龄。不足一年的不计，一年按365天计，不考虑闰年。
	 * @return 返回值都大于等于0，不关心date1和date2的顺序
	 */
	public static int diffYears(Date date1, Date date2) {
		return diffDays(date1, date2) / 365;
	}

	/**
	 * 显示日期date到现在的时间差的字符串友好形式:
	 * 1. 10秒内，显示刚刚
	 * 2. 60秒内，显示xx秒前
	 * 3. 3600秒内，显示xx分钟前
	 * 4. 3600*24秒内，显示xx小时前
	 * 5. 3600*24*10秒内，显示xx天前
	 * 6. 其它显示真实日期，格式yyyy-MM-dd HH:mm
	 */
    public static String getIntervalToNow(Date date) {
        if(date == null){
            return "";
        }
        String interval;
        long seconds = (new Date().getTime() - date.getTime()) / 1000;
        if(seconds >= 0) {
            if (seconds < 10) {
                interval = "刚刚";
            } else if (seconds < 60) {
				interval = seconds + "秒前";
            } else if (seconds < 3600) {
				interval = (seconds / 60) + "分钟前";
            } else if (seconds < 3600 * 24) {
				interval = (seconds / 3600) + "小时前";
            } else if (seconds < 3600 * 24 * 10) {
				interval = (seconds / 3600 / 24) + "天前";
            } else {
				interval = format(date, "yyyy-MM-dd HH:mm");
            }
        } else {
			interval = format(date, "yyyy-MM-dd HH:mm");
        }
        return interval;
    }

	/**
	 * 获取当天的LocalDate格式
	 */
	public static LocalDate today(){
		return DateUtils.toLocalDate(new Date());
	}

	/**
	 * 昨天
	 */
	public static LocalDate yesterday(){
		Date date = new Date();
		return DateUtils.toLocalDate(DateUtils.addTime(date, Calendar.DATE, -1));
	}

	/**
	 * 明天
	 */
	public static LocalDate tomorrow(){
		Date date = new Date();
		return DateUtils.toLocalDate(DateUtils.addTime(date, Calendar.DATE, 1));
	}

	/**
	 * 上周(当天-7的日期)
	 */
	public static LocalDate lastWeek(){
		Date date = new Date();
		return DateUtils.toLocalDate(DateUtils.addTime(date, Calendar.DATE, -7));
	}

	/**
	 * 下周(当天+7的日期)
	 */
	public static LocalDate nextWeek(){
		Date date = new Date();
		return DateUtils.toLocalDate(DateUtils.addTime(date, Calendar.DATE, 7));
	}

	/**
	 * 上月
	 * @return	如当日是2023-06-26，返回结果为2023-05-26
	 */
	public static LocalDate lastMonth(){
		Date date = new Date();
		return DateUtils.toLocalDate(DateUtils.addTime(date, Calendar.MONTH, -1));
	}

	/**
	 * 下月
	 * @return	如当日是2023-06-26，返回结果为2023-07-26
	 */
	public static LocalDate nextMonth(){
		Date date = new Date();
		return DateUtils.toLocalDate(DateUtils.addTime(date, Calendar.MONTH, 1));
	}

}
