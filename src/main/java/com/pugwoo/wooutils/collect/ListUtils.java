package com.pugwoo.wooutils.collect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtils {
	
	/**
	 * 转换list为另一个类型的list
	 * @param list
	 * @param mapper 支持lambda写法
	 * @return
	 */
	public static <T, R> List<R> transform(List<T> list,
			Function<? super T, ? extends R> mapper) {
		if(list == null) {
			return new ArrayList<>();
		}
		return list.stream().map(mapper).collect(Collectors.toList());
	}
	
	/**
	 * filter一个list
	 * @param list
	 * @param predicate
	 * @return
	 */
	public static <T> List<T> filter(List<T> list, Predicate<? super T> predicate) {
		if(list == null) {
			return new ArrayList<>();
		}
		return list.stream().filter(predicate).collect(Collectors.toList());
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
	
	/**
	 * list交集，算法复杂度:n^2
	 */
	public static <E extends Comparable<? super E>> List<E> intersection(List<E> a, List<E> b) {
		if(a == null || b == null) {
			return new ArrayList<>();
		}
		List<E> result = new ArrayList<>();
		for(E e1 : a) {
			for(E e2 : b) {
				if(e1 == e2 || e1.compareTo(e2) == 0) {
					result.add(e1);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * list交集，当a和b中两个元素都有，才放入返回值中。算法复杂度:n^2
	 */
	public static <E> List<E> intersection(List<E> a, List<E> b, Comparator<? super E> c) {
		if(a == null || b == null) {
			return new ArrayList<>();
		}
		List<E> result = new ArrayList<>();
		for(E e1 : a) {
			for(E e2 : b) {
				if(e1 == e2 || c.compare(e1, e2) == 0) {
					result.add(e1);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * list并集，如果b有a没有的，则加入a（不对a和b中的重复元素进行去重）。算法复杂度:n^2
	 */
	public static <E extends Comparable<? super E>> List<E> union(List<E> a, List<E> b) {
		List<E> result = new ArrayList<>();
		if(a != null) {
			result.addAll(a);
		}
		if(a != null && b != null) {
			for(E e1 : b) {
				boolean isExist = false;
				for(E e2 : a) {
					if(e1 == e2 || e1.compareTo(e2) == 0) {
						isExist =  true;
						break;
					}
				}
				if(!isExist) {
					result.add(e1);
				}
			}
		}
		return result;
	}
	
	/**
	 * list并集，如果b有a没有的，则加入a（不对a和b中的重复元素进行去重）。算法复杂度:n^2
	 */
	public static <E> List<E> union(List<E> a, List<E> b, Comparator<? super E> c) {
		List<E> result = new ArrayList<>();
		if(a != null) {
			result.addAll(a);
		}
		if(a != null && b != null) {
			for(E e1 : b) {
				boolean isExist = false;
				for(E e2 : a) {
					if(e1 == e2 || c.compare(e1, e2) == 0) {
						isExist =  true;
						break;
					}
				}
				if(!isExist) {
					result.add(e1);
				}
			}
		}
		return result;
	}

}
