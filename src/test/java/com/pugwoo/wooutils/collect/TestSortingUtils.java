package com.pugwoo.wooutils.collect;

import java.util.*;

public class TestSortingUtils {

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("11");
		list.add("123");
		list.add(null);
		list.add("9");
		
		/**
		 * 你只需要把要排序的对象，转换成已经是Comparable的对象，例如Java自带的int、Date、String等
		 * 
		 * 这个例子，就是按照字符串的长度来排序。
		 */
		SortingUtils.sort(list, new SortingField<String, Integer>(){
			@Override
			public Integer apply(String input) {
				return input.length();
			}
		});
		
		for(String str : list) {
			System.out.println(str);
		}
	}

    // 原生jdk1.8的排序写法
	public void testJdk() {
		// 先定义一个comparator
		Comparator<Map<String, Object>> mapComparator = Comparator
				.<Map<String, Object>>nullsLast((o1, o2) -> 0)
				.thenComparing(o -> Integer.valueOf(o.get("a").toString()), Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(o -> Integer.valueOf(o.get("a").toString()), Comparator.nullsLast(Comparator.naturalOrder()));

		// 然后就可以用这个去排序了
		// Collections.sort();
	}
}
