package com.pugwoo.wooutils.collect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 2016年3月8日 17:59:16 排序工具，元素null safe<br>
 * 
 * 使用方式详见TestSortingUtils
 */
public class SortingUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(SortingUtils.class);

	/**
	 * 排序list。通过SortingField指定排序规则，每个SortingField需要提供一个返回可排序值的函数。<br>
	 * 可排序值例如Integer、String等实现了Comparable接口的类型。<br>
	 * <br>
	 * 特别说明：<br>
	 * - 如果sortingFieldList为null或空，会打印warning log，但不会抛异常，等价于没有排序。<br>
	 * - 如果sortingFieldList中的某个元素为null，在比较时会跳过该排序字段。<br>
	 * 
	 * @param list 排序列表
	 * @param sortingFieldList 排序字段列表，每个SortingField包含排序方向、null值处理方式以及值映射函数
	 */
	public static <T> void sort(List<T> list,
								final List<SortingField<T, ? extends Comparable<?>>> sortingFieldList) {
		if (sortingFieldList == null || sortingFieldList.isEmpty()) {
			LOGGER.warn("sortingFieldList is null or empty, do nothing");
			return;
		}
		if(list == null) {
			return;
		}

		list.sort((left, right) -> {
			for (SortingField<T, ? extends Comparable<?>> sf : sortingFieldList) {
				boolean isLeftNull = left == null;
				boolean isRightNull = right == null;
				if (isLeftNull && isRightNull) {
					continue;
				} else if (isLeftNull) {
					return sf.isNullFirst() ? -1 : 1;
				} else if (isRightNull) {
					return sf.isNullFirst() ? 1 : -1;
				}

				Comparable<Object> comparableLeft = (Comparable<Object>) sf.apply(left);
				Comparable<Object> comparableRight = (Comparable<Object>) sf.apply(right);
				isLeftNull = comparableLeft == null;
				isRightNull = comparableRight == null;
				if (isLeftNull && isRightNull) {
					continue;
				} else if (isLeftNull) {
					return sf.isNullFirst() ? -1 : 1;
				} else if (isRightNull) {
					return sf.isNullFirst() ? 1 : -1;
				}

				int compareResult = comparableLeft.compareTo(comparableRight);
				if (compareResult == 0) {
					continue;
				}
				// 升序时返回原比较结果，降序时返回相反的结果
				return (sf.isAsc() ? 1 : -1) * compareResult;
			}
			return 0;
		});
	}

	/**
	 * 排序list。通过SortingField指定排序规则，每个SortingField需要提供一个返回可排序值的函数。<br>
	 * 可排序值例如Integer、String等实现了Comparable接口的类型。<br>
	 * <br>
	 * 特别说明：<br>
	 * - sortingField参数不能为null，否则会抛出IllegalArgumentException。<br>
	 * - sortingFields参数可以为null或包含null元素，包含null的元素会被忽略。<br>
	 * 
	 * @param list 排序列表
	 * @param sortingField 第一个排序字段，必须提供，不能为null
	 * @param sortingFields 其他排序字段，可选，支持1个或多个，可以为null
	 */
	@SafeVarargs
	public static <T> void sort(List<T> list,
								SortingField<T, ? extends Comparable<?>> sortingField,
								SortingField<T, ? extends Comparable<?>>... sortingFields) {
		if (sortingField == null) {
			throw new IllegalArgumentException("sortingField can not be null");
		}
		if(list == null) {
			return;
		}

		final List<SortingField<T, ? extends Comparable<?>>> sortingFieldList = new ArrayList<>();
		sortingFieldList.add(sortingField);
		if (sortingFields != null) {
			Collections.addAll(sortingFieldList, sortingFields);
		}

		sort(list, sortingFieldList);
	}

	/**
	 * 排序，正序，null值在末尾
	 * @param list 排序列表
	 * @param mapper 排序值映射函数，必须提供，不能为null
	 * @param mappers 其他排序值映射函数，可选
	 */
	@SafeVarargs @SuppressWarnings("unchecked")
	public static <T, R extends Comparable<?>> void sortAscNullLast(List<T> list,
									Function<? super T, ? extends R> mapper,
									Function<? super T, ? extends R>... mappers) {
		if (mapper == null) {
			throw new IllegalArgumentException("mapper can not be null");
		}
		if(list == null) {
			return;
		}

		SortingField<T, R> sortingField = new SortingField<T, R>(SortingOrderEnum.ASC, false) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		};

		SortingField<T, R>[] sortingFields = new SortingField[mappers.length];
		for (int i = 0; i < mappers.length; i++) {
			int finalI = i;
			sortingFields[i] = new SortingField<T, R>(SortingOrderEnum.ASC, false) {
				@Override
				public R apply(T input) {
					return mappers[finalI].apply(input);
				}
			};
		}

		SortingUtils.sort(list, sortingField, sortingFields);
	}

	/**
	 * 排序，正序，null值排前面
	 * @param list 排序列表
	 * @param mapper 排序值映射函数，必须提供，不能为null
	 * @param mappers 其他排序值映射函数，可选
	 */
	@SafeVarargs @SuppressWarnings("unchecked")
	public static <T, R extends Comparable<?>> void sortAscNullFirst(List<T> list,
																	Function<? super T, ? extends R> mapper,
																	Function<? super T, ? extends R>... mappers) {

		if (mapper == null) {
			throw new IllegalArgumentException("mapper can not be null");
		}
		if(list == null) {
			return;
		}

		SortingField<T, R> sortingField = new SortingField<T, R>(SortingOrderEnum.ASC, true) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		};

		SortingField<T, R>[] sortingFields = new SortingField[mappers.length];
		for (int i = 0; i < mappers.length; i++) {
			int finalI = i;
			sortingFields[i] = new SortingField<T, R>(SortingOrderEnum.ASC, true) {
				@Override
				public R apply(T input) {
					return mappers[finalI].apply(input);
				}
			};
		}

		SortingUtils.sort(list, sortingField, sortingFields);
	}

	/**
	 * 排序，逆序，null值在末尾
	 * @param list 排序列表
	 * @param mapper 排序值映射函数，必须提供，不能为null
	 * @param mappers 其他排序值映射函数，可选
	 */
	@SafeVarargs @SuppressWarnings("unchecked")
	public static <T, R extends Comparable<?>> void sortDescNullLast(List<T> list,
																	Function<? super T, ? extends R> mapper,
																	Function<? super T, ? extends R>... mappers) {
		if (mapper == null) {
			throw new IllegalArgumentException("mapper can not be null");
		}
		if(list == null) {
			return;
		}

		SortingField<T, R> sortingField = new SortingField<T, R>(SortingOrderEnum.DESC, false) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		};

		SortingField<T, R>[] sortingFields = new SortingField[mappers.length];
		for (int i = 0; i < mappers.length; i++) {
			int finalI = i;
			sortingFields[i] = new SortingField<T, R>(SortingOrderEnum.DESC, false) {
				@Override
				public R apply(T input) {
					return mappers[finalI].apply(input);
				}
			};
		}

		SortingUtils.sort(list, sortingField, sortingFields);
	}

	/**
	 * 排序，逆序，null值排前面
	 * @param list 排序列表
	 * @param mapper 排序值映射函数，必须提供，不能为null
	 * @param mappers 其他排序值映射函数，可选
	 */
	@SafeVarargs @SuppressWarnings("unchecked")
	public static <T, R extends Comparable<?>> void sortDescNullFirst(List<T> list,
																	 Function<? super T, ? extends R> mapper,
																	 Function<? super T, ? extends R>... mappers) {
		if (mapper == null) {
			throw new IllegalArgumentException("mapper can not be null");
		}
		if(list == null) {
			return;
		}

		SortingField<T, R> sortingField = new SortingField<T, R>(SortingOrderEnum.DESC, true) {
			@Override
			public R apply(T input) {
				return mapper.apply(input);
			}
		};

		SortingField<T, R>[] sortingFields = new SortingField[mappers.length];
		for (int i = 0; i < mappers.length; i++) {
			int finalI = i;
			sortingFields[i] = new SortingField<T, R>(SortingOrderEnum.DESC, true) {
				@Override
				public R apply(T input) {
					return mappers[finalI].apply(input);
				}
			};
		}

		SortingUtils.sort(list, sortingField, sortingFields);
	}
}