package com.pugwoo.wooutils.collect;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TestMapUtils {

	public static void main(String[] args) {
		Map<String, Integer> map = new HashMap<>();
		map.put("you", 6);
		map.put("hello", 5);
		
		map = MapUtils.sortByKey(map);
		for(Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
	}
	
}
