package com.pugwoo.wooutils.date;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.lang.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Date;
import java.util.List;

public class TestDate {

	public static void main(String[] args) throws Exception {
		System.out.println(DateUtils.parse("2017-08-09"));

		System.out.println(DateUtils.getStartTimeOfDay(new Date()));
		System.out.println(DateUtils.getStartTimeOfDay(DateUtils.parse("2017-08-09 01:03:04")));
		System.out.println(DateUtils.getEndTimeOfDay(DateUtils.parse("2017-08-09 01:03:04")));
	}

	private static List<String> testDates = ListUtils.newArrayList(
			"201703", "20170306", "20170306152356",
			"20170306 152356", "2017-03", "2017/03",
			"2017-03-06", "2017/03/06", "16:34:32",
			"16:34", "2017年3月30日", "2017-03-06 15:23",
			"2017/03/06 15:23", "2017-03-06 15:23:56", "2017/03/06 15:23:56",
			"2017-10-18 16:00:00.000",
			"2017-10-18T16:00:00.000Z",
			"2017-10-18T16:00:00.000",
			"2018-10-18T16:00:00.000+0800",
			"2018-10-18T16:00:00.000 +0800",
			"2021-10-18T16:00:00"
	);

    @Test
	public void test() {

		assert DateUtils.parse("0000-00-00") == null;
		assert DateUtils.parse("0000-00-00 00:00:00") == null;
		assert DateUtils.parse(null) == null;
		assert DateUtils.parse("1673961578").equals(DateUtils.parse("2023-01-17 21:19:38"));

		for(String str : testDates) {
			Date date = DateUtils.parse(str);
			System.out.println(date);
			assert date != null;
		}

		for(String str : testDates) {
			LocalDateTime localDt = DateUtils.parseLocalDateTime(str);
		//	System.out.println(localDt);
		//	System.out.println(DateUtils.format(localDt));
		//	System.out.println(DateUtils.formatDate(localDt));
			assert localDt != null;
		}

		for(String str : testDates) {
			LocalDate localDt = DateUtils.parseLocalDate(str);
		//	System.out.println(localDt);
			assert localDt != null;
		}

		for(String str : testDates) {
			LocalTime localDt = DateUtils.parseLocalTime(str);
			//System.out.println(localDt);
			assert localDt != null;
		}

		LocalDate localDate = DateUtils.parseLocalDate("2022-01-01");
		assert localDate.getYear() == 2022;
		assert localDate.getMonth() == Month.JANUARY;
		assert localDate.getDayOfMonth() == 1;

		assert "2022-01-01".equals(DateUtils.format(localDate));

		LocalDateTime localDateTime = DateUtils.parseLocalDateTime("2022-01-01");
		assert "2022-01-01 00:00:00".equals(DateUtils.format(localDateTime));
		localDateTime = DateUtils.parseLocalDateTime("2022-01-01 01:02:03");
		assert "2022-01-01 01:02:03".equals(DateUtils.format(localDateTime));


	}

	@Test
	public void test2() {
		// 测试获取月初和月末
		Date date = DateUtils.parse("2023-05-04");
		LocalDate firstDayOfMonth = DateUtils.getFirstDayOfMonth(date);
		assert DateUtils.formatDate(firstDayOfMonth).equals("2023-05-01");
		LocalDate lastDayOfMonth = DateUtils.getLastDayOfMonth(date);
		assert DateUtils.formatDate(lastDayOfMonth).equals("2023-05-31");

		// 测试获取LocalDate的年月日
		assert DateUtils.getYear(DateUtils.parseLocalDate("2023-04-05")) == 2023;
		assert DateUtils.getMonth(DateUtils.parseLocalDate("2023-04-05")) == 4;
		assert DateUtils.getDay(DateUtils.parseLocalDate("2023-04-05")) == 5;

	}
	
}
