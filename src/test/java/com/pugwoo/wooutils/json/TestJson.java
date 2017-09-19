package com.pugwoo.wooutils.json;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJson {

	public static void main(String[] args) throws Exception {
		Date date = JSON.parse("\"2017-03-03 15:34\"", Date.class);
		System.out.println(date);
		
		date = JSON.parse("\"2017年3月30日\"", Date.class);
		System.out.println(date);
		
		date = JSON.parse("\"  \"", Date.class);
		System.out.println(date);
		
		System.out.println(JSON.toJson(new Date()));
		
		Map<String, Object> map = new HashMap<>();
		map.put("name", "nick");
		map.put("age", null);
		map.put(null, null);
		System.out.println(JSON.toJson(map));
		System.out.println(JSON.toJson(JSON.parse(JSON.toJson(map))));
	}
	
}
