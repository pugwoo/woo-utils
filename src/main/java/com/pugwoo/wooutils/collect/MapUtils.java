package com.pugwoo.wooutils.collect;


import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class MapUtils {

	/**
	 * 转换map的value值为转换后的值
	 * @param map
	 * @param mapper 支持lambda写法
	 */
	public static <K, V1, V2> Map<K, V2> transform(Map<K, V1> map, Function<V1, V2> mapper) {
		if(map == null) {
			return new HashMap<>();
		}
		Map<K, V2> map2 = new HashMap<>();
		for (Map.Entry<K, V1> entry : map.entrySet()) {
			map2.put(entry.getKey(), mapper.apply(entry.getValue()));
		}
		return map2;
	}
	
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
	
	/**
	 * 创建一个map，其值为key、value
	 * @param key
	 * @param value
	 * @return
	 */
	public static Map<String, Object> of(String key, Object value,
			String key2, Object value2, String key3, Object value3,
			String key4, Object value4, String key5, Object value5) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9,
			String key10, Object value10) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
		map.put(key10, value10);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9,
			String key10, Object value10, String key11, Object value11) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
		map.put(key10, value10);
		map.put(key11, value11);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9,
			String key10, Object value10, String key11, Object value11,
			String key12, Object value12) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
		map.put(key10, value10);
		map.put(key11, value11);
		map.put(key12, value12);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9,
			String key10, Object value10, String key11, Object value11,
			String key12, Object value12, String key13, Object value13) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
		map.put(key10, value10);
		map.put(key11, value11);
		map.put(key12, value12);
		map.put(key13, value13);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9,
			String key10, Object value10, String key11, Object value11,
			String key12, Object value12, String key13, Object value13,
			String key14, Object value14) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
		map.put(key10, value10);
		map.put(key11, value11);
		map.put(key12, value12);
		map.put(key13, value13);
		map.put(key14, value14);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9,
			String key10, Object value10, String key11, Object value11,
			String key12, Object value12, String key13, Object value13,
			String key14, Object value14, String key15, Object value15) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
		map.put(key10, value10);
		map.put(key11, value11);
		map.put(key12, value12);
		map.put(key13, value13);
		map.put(key14, value14);
		map.put(key15, value15);
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
			String key4, Object value4, String key5, Object value5,
			String key6, Object value6, String key7, Object value7,
			String key8, Object value8, String key9, Object value9,
			String key10, Object value10, String key11, Object value11,
			String key12, Object value12, String key13, Object value13,
			String key14, Object value14, String key15, Object value15,
			String key16, Object value16) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		map.put(key7, value7);
		map.put(key8, value8);
		map.put(key9, value9);
		map.put(key10, value10);
		map.put(key11, value11);
		map.put(key12, value12);
		map.put(key13, value13);
		map.put(key14, value14);
		map.put(key15, value15);
		map.put(key16, value16);
		return map;
	}
}
