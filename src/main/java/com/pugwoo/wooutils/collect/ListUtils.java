package com.pugwoo.wooutils.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtils {
	
	public static <T, R> List<R> transform(List<T> list,
			Function<? super T, ? extends R> mapper) {
		if(list == null) {
			return new ArrayList<>();
		}
		return list.stream().map(mapper).collect(Collectors.toList());
	}
	
	@SafeVarargs
	public static <E> List<E> newArrayList(E... elements) {
		if(elements == null || elements.length == 0) {
			return new ArrayList<>();
		}
		
		List<E> list = new ArrayList<>(elements.length);
		for(E e : elements) {
			list.add(e);
		}
		
		return list;
	}
	
}
