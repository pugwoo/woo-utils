package com.pugwoo.wooutils.collect;

import java.util.ArrayList;
import java.util.List;

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
}
