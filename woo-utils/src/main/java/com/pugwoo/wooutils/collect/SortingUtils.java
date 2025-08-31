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
	 * 排序list。只需要返回一个可排序的值，例如Integer。
	 * @param list 排序列表
	 * @param sortingFieldList 排序值，特别说明，如果值为空，会打印warning log，但是不会抛异常，等价于没有排序
	 */
	public static <T> void sort(List<T> list,
								final List<SortingField<T, ? extends Comparable<?>>> sortingFieldList) {
		if (sortingFieldList == null || sortingFieldList.isEmpty()) {
			LOGGER.warn("sortingFieldList is null or empty, do nothing");
			return; // 修复：当sortingFieldList为null时应该直接返回，避免空指针异常
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
				return (sf.isAsc() ? 1 : -1) * compareResult;
			}
			return 0;
		});
	}

	/**
	 * 排序list。只需要返回一个可排序的值，例如Integer。
	 * @param list 排序列表
	 * @param sortingField 排序值，这里单独列出是为了让使用者至少传递一个mapper进来
	 * @param sortingFields 排序值，支持1个或多个
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
	 * @param list
	 * @param mapper 排序值，这里单独列出是为了让使用者至少传递一个mapper进来
	 * @param mappers
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
	 * @param list
	 * @param mappers
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
	 * @param list
	 * @param mappers
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
	 * @param list
	 * @param mappers
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
