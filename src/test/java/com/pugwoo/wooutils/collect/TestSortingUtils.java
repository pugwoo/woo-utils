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
