package com.pugwoo.wooutils.collect;

/**
 * 排序字段
 * @param <F>
 * @param <T>
 */
public abstract class SortingField<F, T extends Comparable<?>> {

	/** 升序逆序标记 */
	private boolean isAsc = true;
	
	/** null值在前面还是在后面，默认null值排在最后*/
	private boolean isNullFirst = false;

	public SortingField() {
	}

	/**
	 * 指定排序顺序
	 * @param sortingOrder 当值为ASC，升序排列；当DESC时，逆序排列
	 */
	public SortingField(SortingOrderEnum sortingOrder) {
		this.isAsc = sortingOrder == SortingOrderEnum.ASC;
	}
	
	/**
	 * 指定排序顺序和null值在最前还是最后
	 * @param sortingOrder
	 * @param isNullFirst
	 */
	public SortingField(SortingOrderEnum sortingOrder, boolean isNullFirst) {
		this.isAsc = sortingOrder == SortingOrderEnum.ASC;
		this.isNullFirst = isNullFirst;
	}

	/**
	 * 输入F类型，返回T类型
	 * 
	 * @param input 非null
	 * @return
	 */
	public abstract T apply(F input);

	public boolean isAsc() {
		return isAsc;
	}
	
	public boolean isNullFirst() {
		return isNullFirst;
	}

}