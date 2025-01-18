package com.pugwoo.wooutils.collect;

import java.util.*;
import java.util.function.Function;

/**
 * 提供归并排序获取数据的工具
 */
public class MergeSortUtils {

    private static class ReversedView<E> extends AbstractList<E> {

        private final List<E> backingList;

        public ReversedView(List<E> backingList){
            this.backingList = backingList;
        }

        @Override
        public E get(int i) {
            return backingList.get(backingList.size()-i-1);
        }

        @Override
        public int size() {
            return backingList.size();
        }
    }

    /**
     * @param lists 原数据列表，需要保证数据列表是排序好的，且所有列表要么都正序，要么都是逆序
     * @param isAsc 排序顺序是正序还是逆序
     * @param offsetValue 起始值，如果不存在起始值，则设置为null。查询结果【不包括】该起始值的值
     * @param limit 取的个数
     * @param dupFilter 去重映射，如果为null则表示不去重
     * @return
     */
    public static <T extends MergeItem, R extends Comparable<?>> List<T> merge(
                 List<List<T>> lists,
                 boolean isAsc, Comparable offsetValue, int limit,
                 Function<? super T, ? extends R> dupFilter) {
        if(lists == null || lists.isEmpty()) {
            return new ArrayList<>();
        }

        boolean head = determinateDirection(lists, isAsc); // 是否从头开始
        if(!head) {
            for(int i = 0; i < lists.size(); i++) {
                if(lists.get(i) != null) {
                    lists.set(i, new ReversedView<>(lists.get(i)));
                }
            }
        }
        return getFromHead(lists, isAsc, offsetValue, limit, dupFilter);
    }

    /**
     * 从头部开始获取
     */
    private static <T extends MergeItem, R extends Comparable<?>> List<T> getFromHead(List<List<T>> lists, boolean isAsc,
                   Comparable offsetValue, int limit, Function<? super T, ? extends R> dupFilter) {

        int[] indexes = new int[lists.size()];
        for(int i = 0; i < lists.size(); i++) {
            int index = 0;
            List<T> list = lists.get(i);
            if(offsetValue != null) {
                if(isAsc) {
                    while(index < list.size() && offsetValue.compareTo(list.get(index).getSeq()) >= 0) {
                        index++;
                    }
                } else {
                    while(index < list.size() && offsetValue.compareTo(list.get(index).getSeq()) <= 0) {
                        index++;
                    }
                }
            }
            indexes[i] = index;
        }

        Set<Comparable> addKeys = new HashSet<>();
        List<T> result = new ArrayList<>();
        if(isAsc) {
            for(int i = 0; i < limit; i++) {
                Integer minIndex = null;
                Comparable minOne = null;
                for(int j = 0; j < indexes.length; j++) {
                    int listSize = lists.get(j).size();
                    if(indexes[j] >= listSize) {
                        continue;
                    }
                    Comparable currObj = lists.get(j).get(indexes[j]).getSeq();
                    if(minOne == null || minOne.compareTo(currObj) > 0) {
                        minOne = currObj;
                        minIndex = j;
                    }
                }
                if(minOne == null) {
                    break;
                }

                T t = lists.get(minIndex).get(indexes[minIndex]);
                if(dupFilter != null) {
                    Comparable<?> key = dupFilter.apply(t);
                    if(!addKeys.contains(key)) {
                        result.add(t);
                        addKeys.add(key);
                    } else {
                        i--;
                    }
                } else {
                    result.add(t);
                }
                indexes[minIndex]++;
            }
        } else {
            for(int i = 0; i < limit; i++) {
                Integer maxIndex = null;
                Comparable maxOne = null;
                for(int j = 0; j < indexes.length; j++) {
                    int listSize = lists.get(j).size();
                    if(indexes[j] >= listSize) {
                        continue;
                    }
                    Comparable currObj = lists.get(j).get(indexes[j]).getSeq();
                    if(maxOne == null || maxOne.compareTo(currObj) < 0) {
                        maxOne = currObj;
                        maxIndex = j;
                    }
                }
                if(maxOne == null) {
                    break;
                }

                T t = lists.get(maxIndex).get(indexes[maxIndex]);
                if(dupFilter != null) {
                    Comparable<?> key = dupFilter.apply(t);
                    if(!addKeys.contains(key)) {
                        result.add(t);
                        addKeys.add(key);
                    } else {
                        i--;
                    }
                } else {
                    result.add(t);
                }
                indexes[maxIndex]++;
            }
        }

        return result;
    }

    /**
     * 确定归并排序是从列表头还是尾部开始
     * @param lists
     * @param isAsc
     * @return true则是从头开始，即正序
     */
    private static <T extends MergeItem> boolean determinateDirection(List<List<T>> lists, boolean isAsc) {
        Boolean isListAsc = null; // 列表是否是自增排序
        for(List<T> list : lists) {
            if(list == null || list.size() <= 1) { // list中只有一个值，无法判断方向
                continue;
            }

            MergeItem base = list.get(0);
            for(int i = 1; i < list.size(); i++) {
                int compare = base.getSeq().compareTo(list.get(i).getSeq());
                if(compare > 0) {
                    isListAsc = false;
                } else if (compare < 0) {
                    isListAsc = true;
                }
            }

            if(isListAsc != null) { // 考虑到一整个list其seq都相同的情况
                break;
            }
        }

        if(isListAsc == null) { // 如果列表没有顺序，则没有所谓正逆序
            return true;
        }
        return isAsc ? isListAsc : !isListAsc;
    }

}
