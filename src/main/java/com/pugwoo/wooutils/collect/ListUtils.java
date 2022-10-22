package com.pugwoo.wooutils.collect;

import com.pugwoo.wooutils.lang.NumberUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ListUtils {

	/**
	 * 将数组转换成list，不同于Arrays.asList(array)，这个方法返回的数组可以对其进行修改操作
	 * @param elements 数组
	 */
	@SafeVarargs
	public static <E> List<E> newArrayList(E... elements) {
		if(elements == null || elements.length == 0) {
			return new ArrayList<>();
		}

		List<E> list = new ArrayList<>(elements.length);
		list.addAll(Arrays.asList(elements));

		return list;
	}

    public static <E> List<E> toList(Collection<E> c) {
		if (c == null) {
			return new ArrayList<>();
		}

		List<E> list = new ArrayList<>(c.size());
		list.addAll(c);

		return list;
	}
	
	/**
	 * 排序，正序，null值在末尾
	 * @param list
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortAscNullLast(List<T> list,
																	Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortAscNullLast(list, mappers);
	}
	
	/**
	 * 排序，正序，null值在前面
	 * @param list
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortAscNullFirst(List<T> list,
			Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortAscNullFirst(list, mappers);
	}
	
	/**
	 * 排序，逆序，null值在末尾
	 * @param list
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortDescNullLast(List<T> list,
			Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortDescNullLast(list, mappers);
	}
	
	/**
	 * 排序，逆序，null值在前面
	 * @param list
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortDescNullFirst(List<T> list,
			Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortDescNullFirst(list, mappers);
	}
	
	/**
	 * 转换list为另一个类型的list
	 * @param list
	 * @param mapper 支持lambda写法
	 * @return
	 */
	public static <T, R> List<R> transform(Collection<T> list,
			Function<? super T, ? extends R> mapper) {
		if(list == null) {
			return new ArrayList<>();
		}
		return list.stream().map(mapper).collect(Collectors.toList());
	}

	public static <T, R> List<R> transform(T[] array,
			Function<? super T, ? extends R> mapper) {
		List<R> list = new ArrayList<>();
		if (array != null) {
			for (T t : array) {
				R r = mapper.apply(t);
				list.add(r);
			}
		}
		return list;
	}
	
	/**
	 * 转换list为set
	 * @param list
	 * @param mapper 支持lambda写法
	 * @return
	 */
	public static <T, R> Set<R> toSet(Collection<T> list, Function<? super T, ? extends R> mapper) {
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
	public static <T, K, V> Map<K, V> toMap(Collection<T> list,
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
	public static <T, K, V> Map<K, List<V>> toMapList(Collection<T> list,
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {
		if(list == null) {
			return new HashMap<>();
		}
		Map<K, List<V>> map = new HashMap<>();
		for(T t : list) {
			if(t == null) {continue;}
			K key = keyMapper.apply(t);
			List<V> values = map.computeIfAbsent(key, k -> new ArrayList<>());
			V value = valueMapper.apply(t);
			values.add(value);
		}
		return map;
	}

	/**
	 *  group by （转换list为map）
	 * @param list
	 * @param keyMapper
	 * @param <T>
	 * @param <K>
	 * @return
	 */
	public static <T, K> Map<K, List<T>> groupBy(Collection<T> list, Function<? super T, ? extends K> keyMapper) {
		return toMapList(list,keyMapper,o->o);
	}

	/**
	 * list按指定的数量分组
	 * @param list
	 * @param groupNum 分组的数量，必须大于等于1，当小于1时返回空数组
	 */
	public static <T> List<List<T>> groupByNum(Collection<T> list, final int groupNum) {
		if (list == null || groupNum < 1) {
			return new ArrayList<>();
		}
		
		return  list.stream().collect(new Collector<T, List<List<T>>, List<List<T>>>() {
			// 每组的个数
			private final int number = groupNum;

			@Override
			public Supplier<List<List<T>>> supplier() {
				return ArrayList::new;
			}

			@Override
			public BiConsumer<List<List<T>>, T> accumulator() {
				return (list, item) -> {
					if (list.isEmpty()) {
						list.add(this.createNewList(item));
					} else {
						List<T> last = list.get(list.size() - 1);
						if (last.size() < number) {
							last.add(item);
						} else {
							list.add(this.createNewList(item));
						}
					}
				};
			}

			@Override
			public BinaryOperator<List<List<T>>> combiner() {
				return (list1, list2) -> {
					list1.addAll(list2);
					return list1;
				};
			}

			@Override
			public Function<List<List<T>>, List<List<T>>> finisher() {
				return Function.identity();
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
			}

			private List<T> createNewList(T item) {
				List<T> newOne = new ArrayList<>();
				newOne.add(item);
				return newOne;
			}
		});
	}

	/**
	 * list按指定的数量分组
	 * @param list
	 * @param groupNum 分组的数量，必须大于等于1，当小于1时返回空数组
	 */
	public static <T> List<List<T>> partition(Collection<T> list, final int groupNum) {
		return groupByNum(list, groupNum);
	}

	/**
	 * filter一个list
	 * @param list
	 * @param predicate
	 * @return
	 */
	public static <T> List<T> filter(Collection<T> list, Predicate<? super T> predicate) {
		if(list == null) {
			return new ArrayList<>();
		}
		return list.stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * filter一个数组
	 */
	public static <T> List<T> filter(T[] array, Predicate<? super T> predicate) {
		if (array == null || array.length == 0) {
			return new ArrayList<>();
		}

		List<T> list = new ArrayList<>();
		for (T t : array) {
			if (predicate.test(t)) {
				list.add(t);
			}
		}
		return list;
	}
	
	public static <T> void forEach(Collection<T> list, Consumer<? super T> consumer) {
		if(list != null) {
			for(T t : list) {
				consumer.accept(t);
			}
		}
	}

	public static <T> void forEach(T[] array, Consumer<? super T> consumer) {
		if (array != null) {
			for (T t : array) {
				consumer.accept(t);
			}
		}
	}

	public static boolean isEmpty(Collection<?> list) {
		return list == null || list.isEmpty();
	}

	public static boolean isNotEmpty(Collection<?> list) {
		return !isEmpty(list);
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * list中是否包含有符合条件的元素
	 * @param list
	 * @param predicate
	 * @return
	 */
	public static <T> boolean contains(Collection<T> list, Predicate<? super T> predicate) {
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
	public static <T, R> boolean hasDuplicate(Collection<T> list,
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
		return filter(a, set::contains);
	}

	/**
	 * list交集，返回List a和List b中都有的值，去重，不保证顺序。
	 * 算法时间复杂度:O(n)，空间复杂度O(n)，n是所有lists中的元素总数
	 * @param mapper 实际上是以lambda表达式返回的值进行去重的
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
	 * @param mapper 实际上是以lambda表达式返回的值进行去重的
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
	public static <E> List<E> subtract(List<E> a, List<E> b) {
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
	 * list相减，返回List a中有但是List b中没有的数据，去重，不保证顺序。
	 *
	 * @deprecated 请使用subtract
	 */
	@Deprecated
	public static <E> List<E> sub(List<E> a, List<E> b) {
		return subtract(a, b);
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
		ListUtils.forEach(a, o -> ListUtils.forEach(b, p -> result.add(new MyEntry(o, p))));
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
	 * 随机打乱list
	 */
	public static <E> void shuffle(List<E> list) {
		if (isEmpty(list) || list.size() == 1) {
			return;
		}
		Collections.shuffle(list);
	}

	/**
	 * 打平一个嵌套list
	 */
	public static <E> List<E> flat(List<List<E>> list) {
		if (list == null) {
			return new ArrayList<>();
		}
		return list.stream()
				   .flatMap(List::stream)
				   .collect(Collectors.toList());
	}

	/**
	 * 数值求和
	 * @param list
	 * @param mapper
	 * @return
	 * @deprecated 请使用NumberUtils.sum，该接口将在高版本中删除
	 */
	@Deprecated
	public static <T, R> BigDecimal sum(Collection<T> list, Function<? super T, ? extends R> mapper) {
		return NumberUtils.sum(list, mapper);
	}
	
}
