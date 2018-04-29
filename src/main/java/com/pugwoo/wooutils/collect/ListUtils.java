package com.pugwoo.wooutils.collect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
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
	
	public static <T> void forEach(List<T> list, Consumer<? super T> consumer) {
		if(list != null) {
			for(T t : list) {
				consumer.accept(t);
			}
		}
	}
	
	/**
	 * list中是否包含有符合条件的元素
	 * @param list
	 * @param predicate
	 * @return
	 */
	public static <T> boolean contains(List<T> list, Predicate<? super T> predicate) {
		if(list == null) return false;
		for(T t : list) {
			if(predicate.test(t)) {
				return true;
			}
		}
		return false;
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
	 * list交集，返回List a和List b中都有的值，去重，不保证顺序。
	 * 算法时间复杂度:O(n)，空间复杂度O(n)
	 */
	public static <E> List<E> intersection(List<E> a, List<E> b) {
		if(a == null || b == null || a.isEmpty() || b.isEmpty()) {
			return new ArrayList<>();
		}
		Set<E> set = new HashSet<>(b);
		return filter(a, o -> set.contains(o));
	}

	/**
	 * list并集，返回List a或List b有的值，去重，不保证顺序。
	 * 算法时间复杂度:O(n)，空间复杂度O(n)
	 */
	public static <E> List<E> union(List<E> a, List<E> b) {
		Set<E> result = new HashSet<>();
		if(a != null) {
			result.addAll(a);
		}
		if(b != null) {
			result.addAll(b);
		}
		return new ArrayList<>(result);
	}
	
	/**
	 * list相减，返回List a中有但是List b中没有的数据，去重，不保证顺序。
	 */
	public static <E> List<E> sub(List<E> a, List<E> b) {
		if(a == null || a.isEmpty()) {
			return new ArrayList<>();
		}
		if(b == null || b.isEmpty()) {
			return new ArrayList<>(a);
		}
		Set<E> bSet = new HashSet<>(b);
		return ListUtils.filter(a, o -> !bSet.contains(o));
	}
	
	/**
	 * 返回List a和List b的笛卡尔积列表
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E1, E2> List<Entry<E1, E2>> cartesianProduct(List<E1> a, List<E2> b) {
		if(a == null || a.isEmpty() || b == null || b.isEmpty()) {
			return new ArrayList<>();
		}
		final List<Entry<E1, E2>> result = new ArrayList<>();
		ListUtils.forEach(a, o -> {
			ListUtils.forEach(b, p -> result.add(new MyEntry(o, p)));
		});
		return result;
	}

	static final class MyEntry<K, V> implements Entry<K, V> {
	    private final K key;
	    private V value;
	    public MyEntry(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }
	    @Override
	    public K getKey() {
	        return key;
	    }
	    @Override
	    public V getValue() {
	        return value;
	    }
	    @Override
	    public V setValue(V value) {
	        V old = this.value;
	        this.value = value;
	        return old;
	    }
	}

	/**
	 * 数值求和
	 * @param list
	 * @param mapper
	 * @return
	 */
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
