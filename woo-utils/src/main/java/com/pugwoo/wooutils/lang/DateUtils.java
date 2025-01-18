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
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 特别说明：Date是有时区的，默认使用操作系统的时区。建议使用LocalDateTime，LocalDate，LocalTime，代替Date
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

	public static LocalDateTime toLocalDateTime(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static LocalDate toLocalDate(Date date) {
		if(date == null) {return null;}
		// java.sql.Date和java.sql.Time不支持date.toInstant()
		if (date instanceof java.sql.Date || date instanceof java.sql.Time) {
			date = new Date(date.getTime());
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
	 * 计算两个日期的天数差。同一天返回0，相隔1天返回1，以此类推。
	 * @return 返回值都大于等于0，不关心date1和date2的顺序
	 */
	public static int diffDays(LocalDate date1, LocalDate date2) {
		long between = ChronoUnit.DAYS.between(date1, date2);
		return between >= 0 ? ((int) between) : (-(int) between);
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
	public static LocalDate today() {
		return LocalDate.now();
	}

	/**
	 * 昨天
	 */
	public static LocalDate yesterday() {
		return today().minusDays(1);
	}

	/**
	 * 明天
	 */
	public static LocalDate tomorrow() {
		return today().plusDays(1);
	}

	/**
	 * 上周(当天-7的日期)
	 */
	public static LocalDate lastWeek() {
		return today().minusDays(7);
	}

	/**
	 * 下周(当天+7的日期)
	 */
	public static LocalDate nextWeek() {
		return today().plusDays(7);
	}

	/**
	 * 上月
	 * @return	如当日是2023-06-26，返回结果为2023-05-26
	 */
	public static LocalDate lastMonth() {
		return today().minusMonths(1);
	}

	/**
	 * 下月
	 * @return	如当日是2023-06-26，返回结果为2023-07-26
	 */
	public static LocalDate nextMonth() {
		return today().plusMonths(1);
	}


	// ======================================= 新的LocalDateTime解析器 ===================== START =====================

	public static final Map<String, DateTimeFormatter> LOCAL_TIME_FORMATTER = new LinkedHashMap<String, DateTimeFormatter>() {{
		put("^\\d{1,2}:\\d{1,2}:\\d{1,2}$", DateTimeFormatter.ofPattern("H:m:s")); // 16:34:32
		put("^\\d{1,2}:\\d{1,2}$", DateTimeFormatter.ofPattern("H:m")); // 16:34
		put("^\\d{1,2}:\\d{1,2}Z$", DateTimeFormatter.ofPattern("H:mX")); // 16:34Z

		// 时间带纳秒部分
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.optionalStart().appendPattern("H:m:s").optionalEnd()
				.optionalStart().appendPattern("HHmmss").optionalEnd()
				.optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd() // 毫秒 纳秒 0-9位
				.optionalStart().appendPattern("XXX").optionalEnd()  // 支持 +00:00 格式
				.optionalStart().appendPattern("xxxx").optionalEnd() // 支持 +0000 格式
				.optionalStart().appendPattern("XX").optionalEnd()    // 支持 +00 格式
				.optionalStart().appendPattern("X").optionalEnd()    // 支持 Z 格式
				.optionalStart().appendPattern(" XXX").optionalEnd()  // 支持 " +00:00" 格式
				.optionalStart().appendPattern(" xxxx").optionalEnd() // 支持 " +0000" 格式
				.toFormatter();
		// 16:00:00[.纳秒1-9位][+00:00或+0000或Z]      16:00:00[.纳秒1-9位][+00:00或+0000或Z]
		// 16:00:00[.纳秒1-9位][+00:00或+0000或Z]      16:00:00[.纳秒1-9位][+00:00或+0000或Z]
		put("^\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-](\\d{4}|\\d{2})))?$", formatter);
		put("^\\d{6}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-](\\d{4}|\\d{2})))?$", formatter);
	}};

	public static final Map<String, Boolean> LOCAL_DATE_IS_MONTH = new HashMap<String, Boolean>(){{
		put("^\\d{6}$", true); // 201703
		put("^\\d{4}-\\d{1,2}$", true); // 2017-03
		put("^\\d{4}/\\d{1,2}$", true); // 2017/03
		put("^\\d{4}年\\d{1,2}月$", true); // 2017年03月
	}};

	public static final Map<String, DateTimeFormatter> LOCAL_DATE_FORMATTER = new LinkedHashMap<String, DateTimeFormatter>() {{
		put("^\\d{4}-\\d{1,2}-\\d{1,2}$", DateTimeFormatter.ofPattern("yyyy-M-d")); // 2017-03-06
		put("^\\d{4}/\\d{1,2}/\\d{1,2}$", DateTimeFormatter.ofPattern("yyyy/M/d")); // 2017/03/06
		put("^\\d{8}$", DateTimeFormatter.ofPattern("yyyyMMdd")); // 20170306
		put("^\\d{4}年\\d{1,2}月\\d{1,2}日$", DateTimeFormatter.ofPattern("yyyy年M月d日")); // 2017年03月30日

		put("^\\d{6}$", DateTimeFormatter.ofPattern("yyyyMM-d")); // 201703
		put("^\\d{4}-\\d{1,2}$", DateTimeFormatter.ofPattern("yyyy-M-d")); // 2017-03
		put("^\\d{4}/\\d{1,2}$", DateTimeFormatter.ofPattern("yyyy/M-d")); // 2017/03
		put("^\\d{4}年\\d{1,2}月$", DateTimeFormatter.ofPattern("yyyy年M月-d")); // 2017年03月
	}};

	public static final Map<String, DateTimeFormatter> LOCAL_DATE_TIME_FORMATTER = new LinkedHashMap<String, DateTimeFormatter>() {{

		// 最常用的放前面，提高性能
		put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", DateTimeFormatter.ofPattern("yyyy-M-d H:m:s")); // 2017-03-06 15:23:56

		// 只到分钟：2017-03-06 15:23   2017/03/06 15:23  2017-03-06T15:23   2017/03/06T15:23
		DateTimeFormatter formatterMinute = new DateTimeFormatterBuilder()
				.optionalStart().appendPattern("yyyy-M-d").optionalEnd()
				.optionalStart().appendPattern("yyyy/M/d").optionalEnd()
				.optionalStart().appendLiteral('T').optionalEnd()
				.optionalStart().appendLiteral(' ').optionalEnd()
				.appendPattern("H:m").toFormatter();
		put("^\\d{4}(/\\d{1,2}/|-\\d{1,2}-)\\d{1,2}[T ]\\d{1,2}:\\d{1,2}$", formatterMinute);

		// 其它
		put("^\\d{14}$", DateTimeFormatter.ofPattern("yyyyMMddHHmmss")); // 20170306152356

		// 带毫秒纳秒的时间格式
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.optionalStart().appendPattern("yyyy-M-d").optionalEnd()
				.optionalStart().appendPattern("yyyy/M/d").optionalEnd()
				.optionalStart().appendPattern("yyyyMMdd").optionalEnd()
				.optionalStart().appendLiteral('T').optionalEnd()
				.optionalStart().appendLiteral(' ').optionalEnd()
				.optionalStart().appendPattern("H:m:s").optionalEnd()
				.optionalStart().appendPattern("HHmmss").optionalEnd()
				.optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd() // 毫秒 纳秒 0-9位
				.optionalStart().appendPattern("XXX").optionalEnd()  // 支持 +00:00 格式
				.optionalStart().appendPattern("xxxx").optionalEnd() // 支持 +0000 格式
				.optionalStart().appendPattern("XX").optionalEnd()    // 支持 +00 格式
				.optionalStart().appendPattern("X").optionalEnd()    // 支持 Z 格式
				.optionalStart().appendPattern(" XXX").optionalEnd()  // 支持 " +00:00" 格式
				.optionalStart().appendPattern(" xxxx").optionalEnd() // 支持 " +0000" 格式
				.toFormatter();
		// 2017-10-18T16:00:00[.纳秒1-9位][+00:00或+0000或Z]      2017-10-18 16:00:00[.纳秒1-9位][+00:00或+0000或Z]
		// 2017/10/18T16:00:00[.纳秒1-9位][+00:00或+0000或Z]      2017/10/18 16:00:00[.纳秒1-9位][+00:00或+0000或Z]
		put("^\\d{4}(/\\d{1,2}/|-\\d{1,2}-)\\d{1,2}[T ]\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-](\\d{4}|\\d{2})))?$", formatter);
		// 20171018T160000[.纳秒1-9位][+00:00或+0000或Z]      20171018 160000[.纳秒1-9位][+00:00或+0000或Z]
		put("^\\d{8}[T ]\\d{6}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-](\\d{4}|\\d{2})))?$", formatter);
		put("^\\d{8}[T ]\\d{1,2}:\\d{1,2}:\\d{1,2}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-](\\d{4}|\\d{2})))?$", formatter);
		put("^\\d{4}(/\\d{1,2}/|-\\d{1,2}-)\\d{1,2}[T ]\\d{6}(\\.\\d{0,9})?(Z|( ?[+-]\\d{2}:\\d{2})|( ?[+-](\\d{4}|\\d{2})))?$", formatter);
	}};

	/**解析失败抛异常*/
	public static LocalDateTime parseLocalDateTimeThrowException(String dateString) throws ParseException {
		if (StringTools.isBlank(dateString)) {
			return null;
		}
		dateString = dateString.trim();
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_DATE_TIME_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				return LocalDateTime.parse(dateString, formatter.getValue());
			}
		}

		// 尝试用LocalDate解析，再转成LocalDateTime
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_DATE_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				Boolean isMonth = LOCAL_DATE_IS_MONTH.get(formatter.getKey());
				if (isMonth != null && isMonth) {
					dateString = dateString + "-1";
				}
				LocalDate localDate = LocalDate.parse(dateString, formatter.getValue());
				return localDate.atStartOfDay();
			}
		}

		// 尝试用LocalTime解析，再转成LocalDateTime
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_TIME_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				LocalTime localTime = LocalTime.parse(dateString, formatter.getValue());
				LocalDate localDate = LocalDate.of(0, 1, 1);
				return LocalDateTime.of(localDate, localTime);
			}
		}

		throw new ParseException("Parse failed. Unsupported pattern:" + dateString, 0);
	}


	/**解析失败抛异常*/
	public static LocalDate parseLocalDateThrowException(String dateString) throws ParseException {
		if (StringTools.isBlank(dateString)) {
			return null;
		}
		dateString = dateString.trim();
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_DATE_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				Boolean isMonth = LOCAL_DATE_IS_MONTH.get(formatter.getKey());
				if (isMonth != null && isMonth) {
					dateString = dateString + "-1";
				}
				return LocalDate.parse(dateString, formatter.getValue());
			}
		}

		// 尝试解析成LocalDateTime，再转LocalDate
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_DATE_TIME_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter.getValue());
				return localDateTime.toLocalDate();
			}
		}

		throw new ParseException("Parse failed. Unsupported pattern:" + dateString, 0);
	}

	/**
	 * 解析失败抛异常<br>
	 * 特别说明，即便时间带有时区，也会被忽略，这符合LocalTime语义，如果需要时区，请使用OffsetTime类型
	 */
	public static LocalTime parseLocalTimeThrowException(String dateString) throws ParseException {
		if (StringTools.isBlank(dateString)) {
			return null;
		}
		dateString = dateString.trim();
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_TIME_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				return LocalTime.parse(dateString, formatter.getValue());
			}
		}

		// 尝试解析成LocalDateTime，再转LocalTime
		for (Map.Entry<String, DateTimeFormatter> formatter : LOCAL_DATE_TIME_FORMATTER.entrySet()) {
			if (dateString.matches(formatter.getKey())) {
				LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter.getValue());
				return localDateTime.toLocalTime();
			}
		}

		throw new ParseException("Parse failed. Unsupported pattern:" + dateString, 0);
	}

	/**解析失败抛异常*/
	public static LocalDate parseLocalDateThrowException(String dateString, String pattern) throws ParseException {
		if (StringTools.isBlank(dateString)) {
			return null;
		}
		return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
	}

	/**解析失败不抛异常，返回null*/
	public static LocalDate parseLocalDate(String dateString) {
		try {
			return parseLocalDateThrowException(dateString);
		} catch (ParseException e) {
			LOGGER.error("Parse LocalDate:{} failed", dateString, e);
			return null;
		}
	}

	/**解析失败不抛异常，返回null*/
	public static LocalDate parseLocalDate(String dateString, String pattern) {
		try {
			return parseLocalDateThrowException(dateString, pattern);
		} catch (ParseException e) {
			LOGGER.error("Parse LocalDate:{} failed", dateString, e);
			return null;
		}
	}

	/**解析失败抛异常<br>
	 * 特别说明，即便时间带有时区，也会被忽略，这符合LocalDateTime语义，如果需要时区，请使用OffsetDateTime类型*/
	public static LocalDateTime parseLocalDateTimeThrowException(String dateString, String pattern) throws ParseException {
		if (StringTools.isBlank(dateString)) {
			return null;
		}
		return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
	}

	/**解析失败不抛异常，返回null<br>
	 * 特别说明，即便时间带有时区，也会被忽略，这符合LocalDateTime语义，如果需要时区，请使用OffsetDateTime类型*/
	public static LocalDateTime parseLocalDateTime(String dateString) {
		try {
			return parseLocalDateTimeThrowException(dateString);
		} catch (ParseException e) {
			LOGGER.error("Parse LocalDateTime:{} failed", dateString, e);
			return null;
		}
	}

	/**解析失败不抛异常，返回null<br>
	 * 特别说明，即便时间带有时区，也会被忽略，这符合LocalDateTime语义，如果需要时区，请使用OffsetDateTime类型*/
	public static LocalDateTime parseLocalDateTime(String dateString, String pattern) {
		try {
			return parseLocalDateTimeThrowException(dateString, pattern);
		} catch (ParseException e) {
			LOGGER.error("Parse LocalDateTime:{} failed", dateString, e);
			return null;
		}
	}

	/**解析失败抛异常<br>
	 * 特别说明，即便时间带有时区，也会被忽略，这符合LocalTime语义，如果需要时区，请使用OffsetTime类型*/
	public static LocalTime parseLocalTimeThrowException(String dateString, String pattern) throws ParseException {
		if (StringTools.isBlank(dateString)) {
			return null;
		}
		return LocalTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
	}

	/**解析失败不抛异常，返回null<br>
	 * 特别说明，即便时间带有时区，也会被忽略，这符合LocalTime语义，如果需要时区，请使用OffsetTime类型*/
	public static LocalTime parseLocalTime(String dateString) {
		try {
			return parseLocalTimeThrowException(dateString);
		} catch (ParseException e) {
			LOGGER.error("Parse LocaTime:{} failed", dateString, e);
			return null;
		}
	}

	/**解析失败不抛异常，返回null<br>
	 * 特别说明，即便时间带有时区，也会被忽略，这符合LocalTime语义，如果需要时区，请使用OOffsetTime类型*/
	public static LocalTime parseLocalTime(String dateString, String pattern) {
		try {
			return parseLocalTimeThrowException(dateString, pattern);
		} catch (ParseException e) {
			LOGGER.error("Parse LocalTime:{} failed", dateString, e);
			return null;
		}
	}
}
