package com.pugwoo.wooutils.collect;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TestMapUtils {

    @Test
	public void testTransform() {
		Map<String, String> map = new HashMap<>();
		map.put("one", "1");
		map.put("two", "22");
		map.put("three", "333");

		Map<String, Integer> transform = MapUtils.transform(map, String::length);

		assert transform.get("one").equals(1);
		assert transform.get("two").equals(2);
		assert transform.get("three").equals(3);
	}

	@Test
	public void sortByKeyTest() {
		List<String> keyList = Stream.of("hello", "you", "world").collect(toList());
		List<String> expectantSortedAscKeyList = keyList.stream().sorted(String::compareTo).collect(toList());
		List<String> expectantSortedDescKeyList = keyList.stream().sorted(Comparator.reverseOrder()).collect(toList());
		
		Map<String, Integer> map = new HashMap<>();
		for (String key : keyList) {
			map.put(key, map.size());
		}
		
		System.out.println("\n ========== source map: ");
		for(Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		
		map = MapUtils.sortByKey(map, false);
		List<String> sortedAscKeyList = new ArrayList<>();
		System.out.println("\n ========== sorted asc map: ");
		for(Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
			sortedAscKeyList.add(entry.getKey());
		}
		assert !sortedAscKeyList.equals(keyList);
		assert sortedAscKeyList.equals(expectantSortedAscKeyList);
		
		map = MapUtils.sortByKey(map, true);
		List<String> sortedDescKeyList = new ArrayList<>();
		System.out.println("\n ========== sorted desc map: ");
		for(Entry<String, Integer> entry : map.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
			sortedDescKeyList.add(entry.getKey());
		}
		assert !sortedDescKeyList.equals(keyList);
		assert sortedDescKeyList.equals(expectantSortedDescKeyList);
	}
	
}
