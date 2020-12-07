package com.pugwoo.wooutils.collect;

import com.pugwoo.wooutils.lang.NumberUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtils {

	/**
	 * 将数组转换成list，不同于Arrays.asList(array)，这个方法返回的数组可以对其进行修改操作
	 * @param array
	 * @param <T>
	 * @return
	 */
	public static <T> List<T> toList(T[] array) {
		if(array == null || array.length == 0) {
			return new ArrayList<>();
		}
		List<T> list = new ArrayList<>(array.length);
		for(T t : array) {
			list.add(t);
		}
		return list;
	}
	
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
	 * 转换list为set
	 * @param list
	 * @param mapper 支持lambda写法
	 * @return
	 */
	public static <T, R> Set<R> toSet(List<T> list, Function<? super T, ? extends R> mapper) {
		if(list == null) {
			return new HashSet<>();
		}
		return list.stream().map(mapper).collect(Collectors.toSet());
	}
	
	/**
	 * 转换list为map,返回的是LinkedHashMap，顺序和list一样。如果key相同，值会被最后一个覆盖。
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
			if(t == null) {continue;}
			map.put(keyMapper.apply(t), valueMapper.apply(t));
		}
		return map;
	}

	/**
	 * 转换list为map
	 * @param list
	 * @param keyMapper
	 * @param valueMapper
	 * @return
	 */
	public static <T, K, V> Map<K, List<V>> toMapList(List<T> list,
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {
		if(list == null) {
			return new HashMap<>();
		}
		Map<K, List<V>> map = new HashMap<>();
		for(T t : list) {
			if(t == null) {continue;}
			K key = keyMapper.apply(t);
			List<V> values = map.get(key);
			if(values == null) {
				values = new ArrayList<>();
				map.put(key, values);
			}
			V value = valueMapper.apply(t);
			values.add(value);
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

	public static boolean isEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}

	public static boolean isNotEmpty(List<?> list) {
		return !isEmpty(list);
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
	
	/**
	 * list中mapper映射的值是否有重复，【不包括null值的比较，null值不包括在重复判断中】
	 * @param list
	 * @param mapper
	 * @return
	 */
	public static <T, R> boolean hasDuplicate(List<T> list,
			Function<? super T, ? extends R> mapper) {
		Set<R> sets = new HashSet<>();
		for(T t : list) {
			if(t == null) continue;
			R r = mapper.apply(t);
			if(r == null) continue;
			if(sets.contains(r)) {
				return true;
			}
			sets.add(r);
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
	 * 算法时间复杂度:O(n)，空间复杂度O(n)，n是所有lists中的元素总数
	 */
	public static <E> List<E> intersection(List<E>... lists) {

		if(lists == null) {return new ArrayList<>();}
		if(lists.length == 1) {return lists[0] != null ? lists[0] : new ArrayList<>();}

		List<E> last = _intersection(lists[0], lists[1]);
		for(int i = 2; i < lists.length; i++) {
			last = _intersection(last, lists[i]);
		}
		return last;
	}

	private static <E> List<E> _intersection(List<E> a, List<E> b) {
		if(a == null || b == null || a.isEmpty() || b.isEmpty()) {
			return new ArrayList<>();
		}
		Set<E> set = new HashSet<>(b);
		return filter(a, o -> set.contains(o));
	}

	/**
	 * list交集，返回List a和List b中都有的值，去重，不保证顺序。
	 * 算法时间复杂度:O(n)，空间复杂度O(n)，n是所有lists中的元素总数
	 * @param mapper 实际上是以lamda表达式返回的值进行去重的
	 */
	public static <E, R extends Comparable<?>> List<E> intersection(
			Function<? super E, ? extends R> mapper, List<E>... lists) {

		if(lists == null) {return new ArrayList<>();}
		if(lists.length == 1) {return lists[0] != null ? lists[0] : new ArrayList<>();}

		List<E> last = _intersection(mapper, lists[0], lists[1]);
		for(int i = 2; i < lists.length; i++) {
			last = _intersection(mapper, last, lists[i]);
		}
		return last;
	}

	private static <E, R extends Comparable<?>> List<E> _intersection(
			Function<? super E, ? extends R> mapper, List<E> a, List<E> b) {
		if(a == null || b == null || a.isEmpty() || b.isEmpty()) {
			return new ArrayList<>();
		}

		Set<R> set = new HashSet<>();
		for(E e : b) {
			set.add(mapper.apply(e));
		}
		return filter(a, o -> set.contains(mapper.apply(o)));
	}

	/**
	 * list并集，返回lists中有的值，去重，不保证顺序。
	 * 算法时间复杂度:O(n)，空间复杂度O(n)，n是所有lists中的元素总数
	 */
	public static <E> List<E> union(List<E>... lists) {

		if(lists == null) {return new ArrayList<>();}
		if(lists.length == 1) {return lists[0] != null ? lists[0] : new ArrayList<>();}

		Set<E> result = new HashSet<>();
		for(List<E> list : lists) {
			if(list != null) {
				result.addAll(list);
			}
		}
		return new ArrayList<>(result);
	}

	/**
	 * list并集，返回lists中有的值，去重，不保证顺序。
	 * 算法时间复杂度:O(n)，空间复杂度O(n)，n是所有lists中的元素总数
	 * @param mapper 实际上是以lamda表达式返回的值进行去重的
	 */
	public static <E, R extends Comparable<?>> List<E> union(
			Function<? super E, ? extends R> mapper, List<E>... lists) {

		if(lists == null) {return new ArrayList<>();}
		if(lists.length == 1) {return lists[0] != null ? lists[0] : new ArrayList<>();}

		Set<R> dup = new HashSet<>();
		List<E> result = new ArrayList<>();

		for(List<E> list : lists) {
			if(list == null) {continue;}
			for(E e : list) {
				R dupId = mapper.apply(e);
				if(!dup.contains(dupId)) {
					dup.add(dupId);
					result.add(e);
				}
			}
		}

		return result;
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
	 * @deprecated 请使用NumberUtils.sum，该接口将在高版本中删除
	 */
	@Deprecated
	public static <T, R> BigDecimal sum(List<T> list, Function<? super T, ? extends R> mapper) {
		return NumberUtils.sum(list, mapper);
	}
	
}
