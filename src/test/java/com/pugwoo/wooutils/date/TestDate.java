package com.pugwoo.wooutils.date;

import java.util.Calendar;

import com.pugwoo.wooutils.date.Date;

public class TestDate {

	public static void main(String[] args) {
		Date date = new Date();
		System.out.println(date);
		
		date = Date.valueOf("2015-01-02 00:12:21");
		System.out.println(date);
		
		date = Date.valueOf("2011-03-04", "yyyy-MM-dd");
		System.out.println(date);
		
		Date date2 = Date.valueOf("2011年11月11日", "yyyy年MM月dd日");
		System.out.println(date2);
		
		System.out.println(date2.format(Date.MYSQL_DATETIME_FORMAT));
		
		date2.addTime(Calendar.DATE, 10);
		System.out.println(date2);
	}
	
}
