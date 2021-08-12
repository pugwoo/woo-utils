package com.pugwoo.wooutils.date;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.lang.DateUtils;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

	}

	
}
