package com.pugwoo.wooutils.collect;

import com.pugwoo.wooutils.lang.NumberUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

	/**
	 * 将数组转换成list，不同于Arrays.asList(array)，这个方法返回的数组可以对其进行修改操作
	 * @param elements 数组
	 */
	@SafeVarargs
	public static <E> List<E> newList(E... elements) {
		return newArrayList(elements);
	}

    public static <E> List<E> toList(Collection<E> c) {
		if (c == null) {
			return new ArrayList<>();
		}

		List<E> list = new ArrayList<>(c.size());
		list.addAll(c);
		return list;
	}

	public static <E> List<E> toList(Stream<E> stream) {
		if (stream == null) {
			return new ArrayList<>();
		}
		return stream.collect(Collectors.toList());
	}
	
	/**
	 * 排序，正序，null值在末尾
	 * @param list
	 * @param mapper 至少需要传递一个mapper进来
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortAscNullLast(List<T> list,
					Function<? super T, ? extends R> mapper,
					Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortAscNullLast(list, mapper, mappers);
	}
	
	/**
	 * 排序，正序，null值在前面
	 * @param list
	 * @param mapper 至少需要传递一个mapper进来
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortAscNullFirst(List<T> list,
			Function<? super T, ? extends R> mapper,
			Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortAscNullFirst(list, mapper, mappers);
	}
	
	/**
	 * 排序，逆序，null值在末尾
	 * @param list
	 * @param mapper 至少需要传递一个mapper进来
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortDescNullLast(List<T> list,
			Function<? super T, ? extends R> mapper,
			Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortDescNullLast(list, mapper, mappers);
	}
	
	/**
	 * 排序，逆序，null值在前面
	 * @param list
	 * @param mapper 至少需要传递一个mapper进来
	 * @param mappers
	 */
	@SafeVarargs
	public static <T, R extends Comparable<?>> void sortDescNullFirst(List<T> list,
			Function<? super T, ? extends R> mapper,
			Function<? super T, ? extends R>... mappers) {
		SortingUtils.sortDescNullFirst(list, mapper, mappers);
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
	 * @param mapper 支持lambda写法
	 */
	public static <T, R> Set<R> toSet(Collection<T> list, Function<? super T, ? extends R> mapper) {
		if(list == null) {
			return new HashSet<>();
		}
		return list.stream().map(mapper).collect(Collectors.toSet());
	}
	
	/**
	 * 转换list为map
	 */
	public static <T, K, V> Map<K, V> toMap(Collection<T> list,
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {
		if(list == null) {
			return new HashMap<>();
		}
		Map<K, V> map = new HashMap<>();
		for(T t : list) {
			if(t == null) {continue;}
			map.put(keyMapper.apply(t), valueMapper.apply(t));
		}
		return map;
	}

	/**
	 * 转换list为map
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
	 */
	public static <T, K> Map<K, List<T>> groupBy(Collection<T> list, Function<? super T, ? extends K> keyMapper) {
		return toMapList(list,keyMapper,o->o);
	}

	/**
	 * stream按指定的数量分组，并返回stream<br/>
	 * 代码来源：<a href="https://stackoverflow.com/questions/32434592/partition-a-java-8-stream">...</a>
	 * @param groupNum 分组的数量，必须大于等于1。0等价于不分组(即groupNum无限大)
	 */
	public static <T> Stream<List<T>> partition(Stream<T> stream, int groupNum) {
		if (stream == null) {
			return Stream.empty();
		}
		List<List<T>> currentBatch = new ArrayList<>(); //just to make it mutable
		currentBatch.add(new ArrayList<>(groupNum));
		return Stream.concat(stream
				.sequential()
				.map(t -> {
					currentBatch.get(0).add(t);
					return currentBatch.get(0).size() == groupNum ? currentBatch.set(0,new ArrayList<>(groupNum)) : null;
				}), Stream.generate(() -> currentBatch.get(0).isEmpty() ? null : currentBatch.get(0))
				.limit(1)
		).filter(Objects::nonNull);
	}

	/**
	 * stream按指定的数量分组，并返回stream
	 * @param stream
	 * @param groupNum 分组的数量，必须大于等于1。0等价于不分组(即groupNum无限大)
	 */
	public static <T> Stream<List<T>> groupByNum(Stream<T> stream, final int groupNum) {
		return partition(stream, groupNum);
	}

	/**
	 * list按指定的数量分组
	 * @param list 这里明确用List类型，不支持Collection
	 * @param groupNum 分组的数量，必须大于等于1，当小于1时返回空数组
	 */
	public static <T> List<List<T>> groupByNum(List<T> list, final int groupNum) {
		if (list == null || groupNum < 1) {
			return new ArrayList<>();
		}

		return IntStream.range(0, getNumberOfPartitions(list, groupNum))
				.mapToObj(i -> list.subList(i * groupNum, Math.min((i + 1) * groupNum, list.size())))
				.collect(Collectors.toList());
	}

	private static <T> int getNumberOfPartitions(Collection<T> list, int batchSize) {
		return (list.size() + batchSize- 1) / batchSize;
	}

	/**
	 * list按指定的数量分组
	 * @param list 这里明确用List类型，不支持Collection
	 * @param groupNum 分组的数量，必须大于等于1，当小于1时返回空数组
	 */
	public static <T> List<List<T>> partition(List<T> list, final int groupNum) {
		return groupByNum(list, groupNum);
	}

	public static <T> List<List<T>> groupByNum(Set<T> set, final int groupNum) {
		if (set == null) {
			return new ArrayList<>();
		}
		return groupByNum(set.stream(), groupNum).collect(Collectors.toList());
	}

	public static <T> List<List<T>> partition(Set<T> set, final int groupNum) {
		return groupByNum(set, groupNum);
	}

	/**
	 * filter一个list
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

	public static <T> void forEach(Stream<T> stream, Consumer<? super T> consumer) {
		if(stream != null) {
			stream.forEach(consumer);
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

	public static <T> boolean hasDuplicate(Collection<T> list) {
		return hasDuplicate(list, Function.identity());
	}
	
	/**
	 * list中mapper映射的值是否有重复，【不包括null值的比较，null值不包括在重复判断中】
	 */
	public static <T, R> boolean hasDuplicate(Collection<T> list,
			Function<? super T, ? extends R> mapper) {
		if (list == null) {
			return false;
		}
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

	public static <T> Map<T, Integer> getDuplicates(Collection<T> list) {
		return getDuplicates(list, Function.identity());
	}

	/**
	 * 获得list中mapper映射的值的重复次数【不包括null值的比较，null值不包括在重复判断中】
	 */
	public static <T, R> Map<R, Integer> getDuplicates(Collection<T> list,
		    Function<? super T, ? extends R> mapper) {
		if (list == null) {
			return new HashMap<>();
		}

		Map<R, Integer> map = new HashMap<>();
		for(T t : list) {
			if(t == null) continue;
			R r = mapper.apply(t);
			if(r == null) continue;
			Integer count = map.get(r);
			if(count == null) {
				count = 0;
			}
			map.put(r, count + 1);
		}

		// 去掉重复次数为1的
		map.entrySet().removeIf(entry -> entry.getValue() <= 1);

		return map;
	}

	/**
	 * 按相同的key合并两个list，返回合并后的list
	 * @param merger 第一个入参是key相同的list1的值，第二个入参是key相同的list2的值，返回值是合并后的值
	 *               注意：merger的入参可能为null，需要判断
	 */
	public static <T, U, R> List<R> merge(List<T> list1, List<U> list2,
											Function<T, String> keyMapper1,
											Function<U, String> keyMapper2,
	                                        BiFunction<List<T>, List<U>, R> merger) {
		Map<String, List<T>> map1 = toMapList(list1, keyMapper1, o -> o);
		Map<String, List<U>> map2 = toMapList(list2, keyMapper2, o -> o);
		List<R> result = new ArrayList<>();
		for (Entry<String, List<T>> e : map1.entrySet()) {
			List<U> list = map2.get(e.getKey());
			result.add(merger.apply(e.getValue(), list));
			if (list != null) {
				map2.remove(e.getKey());
			}
		}

		for (Entry<String, List<U>> e : map2.entrySet()) {
			result.add(merger.apply(null, e.getValue()));
		}

		return result;
	}

	/**
	 * list交集，返回List a和List b中都有的值，去重，不保证顺序。
	 * 算法时间复杂度:O(n)，空间复杂度O(n)，n是所有lists中的元素总数
	 */
	@SafeVarargs
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
	@SafeVarargs
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
	@SafeVarargs
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
	@SafeVarargs
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
	 * 将多个数组合并成一个数组
	 */
	public static Object[] concatArray(Object[] ...objs) {
		if (objs == null || objs.length == 0) {
			return new Object[0];
		}
		int size = 0;
		for (Object[] obj : objs) {
			size += obj == null ? 0 : obj.length;
		}
		Object[] result = new Object[size];
		int current = 0;
		for (Object[] obj : objs) {
			if (obj == null) {
				continue;
			}
			for (Object o : obj) {
				result[current++] = o;
			}
		}
		return result;
	}

	/**
	 * 将多个list合并成一个list，并以Stream的方式返回。
	 * 这里用Stream的考虑是，本来这种写法就是为了避免创建新的list，如果返回的是一个新的list，那么就没有意义了。
	 */
	@SafeVarargs
	public static <T> Stream<T> concat(List<T> ...list) {
		if (list == null || list.length == 0) {
			return Stream.empty();
		}
		return Stream.of(list).flatMap(Collection::stream);
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
