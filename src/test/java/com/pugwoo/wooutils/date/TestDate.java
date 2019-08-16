package com.pugwoo.wooutils.date;

import com.pugwoo.wooutils.lang.DateUtils;

import java.util.Date;

public class TestDate {

	public static void main(String[] args) {
		System.out.println(DateUtils.parse("2017-08-09"));

		System.out.println(DateUtils.getStartTimeOfDay(new Date()));
		System.out.println(DateUtils.getStartTimeOfDay(DateUtils.parse("2017-08-09 01:03:04")));
		System.out.println(DateUtils.getEndTimeOfDay(DateUtils.parse("2017-08-09 01:03:04")));
	}
	
}
