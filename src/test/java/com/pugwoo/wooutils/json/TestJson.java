package com.pugwoo.wooutils.json;

import java.util.Date;

public class TestJson {

	public static void main(String[] args) throws Exception {
		Date date = JSON.parse("\"2017-03-03 15:34\"", Date.class);
		System.out.println(date);
		
		date = JSON.parse("\"2017年3月30日\"", Date.class);
		System.out.println(date);
		
		date = JSON.parse("\"  \"", Date.class);
		System.out.println(date);
		
		System.out.println(JSON.toJson(new Date()));
	}
	
}
