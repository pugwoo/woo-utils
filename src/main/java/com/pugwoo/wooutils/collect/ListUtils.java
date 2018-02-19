package com.pugwoo.wooutils.collect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtils {
	
	/**
	 * 排序，正序，null值在末尾
	 * @param list
	 * @param mapper
	 */
	public static <T, R extends Comparable<?>> void sortAscNullLast(List<T> list,
			Function<? super T, ? extends R> mapper) {
		SortingUtils.sort(list, new SortingField<T, R>(SortingOrderEnum.ASC, false) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		});
	}
	
	/**
	 * 排序，正序，null值在前面
	 * @param list
	 * @param mapper
	 */
	public static <T, R extends Comparable<?>> void sortAscNullFirst(List<T> list,
			Function<? super T, ? extends R> mapper) {
		SortingUtils.sort(list, new SortingField<T, R>(SortingOrderEnum.ASC, true) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		});
	}
	
	/**
	 * 排序，逆序，null值在末尾
	 * @param list
	 * @param mapper
	 */
	public static <T, R extends Comparable<?>> void sortDescNullLast(List<T> list,
			Function<? super T, ? extends R> mapper) {
		SortingUtils.sort(list, new SortingField<T, R>(SortingOrderEnum.DESC, false) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		});
	}
	
	/**
	 * 排序，逆序，null值在前面
	 * @param list
	 * @param mapper
	 */
	public static <T, R extends Comparable<?>> void sortDescNullFirst(List<T> list,
			Function<? super T, ? extends R> mapper) {
		SortingUtils.sort(list, new SortingField<T, R>(SortingOrderEnum.DESC, true) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		});
	}
	
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
	 * 转换list为map,返回的是LinkedHashMap，顺序和list一样
	 * @param list
	 * @param keyMapper
	 * @param valueMapper
	 * @return
	 */
	public static <T, K, V> Map<K, V> toMap(List<T> list,
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {
		if(list == null) {
			return new LinkedHashMap<>();
		}
		Map<K, V> map = new LinkedHashMap<>();
		for(T t : list) {
			if(t == null) continue;
			map.put(keyMapper.apply(t), valueMapper.apply(t));
		}
		return map;
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
	
	public static <T, R> BigDecimal sum(List<T> list, Function<? super T, ? extends R> mapper) {
		BigDecimal sum = BigDecimal.ZERO;
		if(list == null) {
			return sum;
		}
		for(T t : list) {
			if(t == null) continue;
			Object val = mapper.apply(t);
			if(val == null) continue;
			BigDecimal a = null;
			if(!(val instanceof BigDecimal)) {
				try {
					a = new BigDecimal(val.toString());
				} catch (Exception e) {// ignore
				}
			} else {
				a = (BigDecimal) val;
			}
			if(a == null) continue;
			sum = sum.add(a);
		}
		return sum;
	}
	
}
