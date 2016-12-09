package com.pugwoo.wooutils.collect;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 2016年3月8日 17:59:16 排序工具，元素null safe<br>
 * 
 * 使用方式很简单，例如按照String的length长度排序：
 * List<String> list = 创建一个List包含String元素，String元素可以为null，默认null排在最后
 * SortingUtils.sort(list, new SortingField<String, Integer>(){
			@Override
			public Integer apply(String input) { // input不会为null
				return input.length();
			}
 * });
 */
public class SortingUtils {

	/**
	 * 排序list。只需要返回一个可排序的值，例如Integer。
	 * @param list
	 * @param sortingField
	 */
	public static <T, R extends Comparable<R>> void sort(List<T> list, final SortingField<T, R> sortingField) {
		if(list == null || sortingField == null) {
			return;
		}
		
		Collections.sort(list, new Comparator<T>() {
			@Override
			public int compare(T left, T right) {
				boolean isLeftNull = left == null;
				boolean isRightNull = right == null;
				if(isLeftNull && isRightNull) {
					return 0;
				} else if (isLeftNull) {
					return sortingField.isNullFirst() ? -1 : 1;
				} else if (isRightNull) {
					return sortingField.isNullFirst() ? 1 : -1;
				}
				
				R comparableLeft = sortingField.apply(left);
				R comparableRight = sortingField.apply(right);
				isLeftNull = comparableLeft == null;
				isRightNull = comparableRight == null;
				if(isLeftNull && isRightNull) {
					return 0;
				} else if (isLeftNull) {
					return sortingField.isNullFirst() ? -1 : 1;
				} else if (isRightNull) {
					return sortingField.isNullFirst() ? 1 : -1;
				}
				
				return sortingField.isAsc() ? comparableLeft.compareTo(comparableRight) 
						: comparableRight.compareTo(comparableLeft);
			}
		});
	}
	
	public static <T> void sort(List<T> list, final SortingField<T, ? extends Comparable<?>>... sortingFields) {
		if(list == null || sortingFields == null || sortingFields.length == 0) {
			return;
		}
		
		Collections.sort(list, new Comparator<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(T left, T right) {
				for(SortingField<T, ? extends Comparable<?>> sortingField : sortingFields) {
					boolean isLeftNull = left == null;
					boolean isRightNull = right == null;
					if(isLeftNull && isRightNull) {
						continue;
					} else if (isLeftNull) {
						return sortingField.isNullFirst() ? -1 : 1;
					} else if (isRightNull) {
						return sortingField.isNullFirst() ? 1 : -1;
					}
					
					Comparable<Object> comparableLeft = (Comparable<Object>) sortingField.apply(left);
					Comparable<Object> comparableRight = (Comparable<Object>) sortingField.apply(right);
					isLeftNull = comparableLeft == null;
					isRightNull = comparableRight == null;
					if(isLeftNull && isRightNull) {
						continue;
					} else if (isLeftNull) {
						return sortingField.isNullFirst() ? -1 : 1;
					} else if (isRightNull) {
						return sortingField.isNullFirst() ? 1 : -1;
					}
					
					int compareResult = comparableLeft.compareTo(comparableRight);
					if(compareResult == 0) {
						continue;
					}
					return (sortingField.isAsc() ? 1 : -1) * compareResult;
				}
				return 0;
			}
		});
	}

}
