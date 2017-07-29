package com.pugwoo.wooutils.collect;


import java.util.HashMap;
import java.util.Map;

public class MapUtils {

	/**
	 * 创建一个map，其值为key、value
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, Object> of(String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		return map;
	}
	
	/**
	 * 创建一个map，其值为key、value
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, Object> of(String key, Object value, String key2, Object value2) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		return map;
	}
	
	/**
	 * 创建一个map，其值为key、value
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, Object> of(String key, Object value,
			String key2, Object value2, String key3, Object value3) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		return map;
	}
	
}
