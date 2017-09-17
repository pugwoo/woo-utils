package com.pugwoo.wooutils.collect;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {
	
	/**
	 * 按map的key正序排列
	 * @param map
	 * @return
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {
		return sortByKey(map, false);
	}
	
	/**
	 * 按Map的key排序
	 * @param map
	 * @param isDesc 是否逆序,true则为逆序;false为正序
	 * @return 返回的是一个LinkedHashMap
	 */
	public static <K extends Comparable<? super K>, V>
	    Map<K, V> sortByKey(Map<K, V> map, boolean isDesc) {
		Map<K, V> result = new LinkedHashMap<>();
		List<K> keys = new ArrayList<>(map.keySet());
		Collections.sort(keys);
		if(isDesc) {
			Collections.reverse(keys);
		}
		for(K key : keys) {
			result.put(key, map.get(key));
		}
			
		return result;
	}

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
	
	/**
	 * 创建一个map，其值为key、value
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, Object> of(String key, Object value,
			String key2, Object value2, String key3, Object value3,
			String key4, Object value4) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		return map;
	}
	
}
