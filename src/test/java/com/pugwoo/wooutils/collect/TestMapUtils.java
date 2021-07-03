package com.pugwoo.wooutils.collect;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TestMapUtils {

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
