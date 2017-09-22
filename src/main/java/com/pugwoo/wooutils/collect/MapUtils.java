package com.pugwoo.wooutils.collect;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtils {
	
	/**
	 * 按Map的key排序。对于null值，无论正序或逆序，都排最后。
	 * @param map
	 * @param isDesc 是否逆序,true则为逆序;false为正序
	 * @return 返回的是一个LinkedHashMap
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(
			Map<K, V> map, boolean isDesc) {
		if(map == null || map.isEmpty()) {
			return new LinkedHashMap<>();
		}
		Map<K, V> result = new LinkedHashMap<>();
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<K, V>>() {
			@Override public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				if(o1.getKey() == o2.getKey()) {
					return 0;
				}
				if(o1.getKey() == null) {
					return 1;
				}
				if(o2.getKey() == null) {
					return -1;
				}
				int o = o1.getKey().compareTo(o2.getKey());
				return o * (isDesc ? -1 : 1);
			}
		});
		
		for(Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * 按Map的value排序。对于null值，无论正序或逆序，都排最后。
	 * @param map
	 * @param isDesc 是否逆序,true则为逆序;false为正序
	 * @return 返回的是一个LinkedHashMap
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map, boolean isDesc) {
		if(map == null || map.isEmpty()) {
			return new LinkedHashMap<>();
		}
		Map<K, V> result = new LinkedHashMap<>();
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<K, V>>() {
			@Override public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				if(o1.getValue() == o2.getValue()) {
					return 0;
				}
				if(o1.getValue() == null) {
					return 1;
				}
				if(o2.getValue() == null) {
					return -1;
				}
				int o = o1.getValue().compareTo(o2.getValue());
				return o * (isDesc ? -1 : 1);
			}
		});
		
		for(Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
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
