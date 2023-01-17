package com.pugwoo.wooutils.collect;

import com.pugwoo.wooutils.lang.NumberUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestListUtils {

    @Test
    public void testNewArrayList() {
        List<Integer> list = ListUtils.newArrayList(1, 2, 3);
        assert list.size() == 3;
        assert list.get(0).equals(1);
        assert list.get(1).equals(2);
        assert list.get(2).equals(3);

        list = ListUtils.newList(1, 2, 3);
        assert list.size() == 3;
        assert list.get(0).equals(1);
        assert list.get(1).equals(2);
        assert list.get(2).equals(3);

    }

    @Test
    public void testToList() {
        Set<Integer> sets = new HashSet<>();
        sets.add(1);
        sets.add(2);
        sets.add(3);
        List<Integer> list2 = ListUtils.toList(sets);

        assert list2.size() == 3;
        assert list2.contains(1);
        assert list2.contains(2);
        assert list2.contains(3);

        Stream<Integer> stream = list2.stream();
        List<Integer> list3 = ListUtils.toList(stream);
        assert list2.get(0).equals(list3.get(0));
        assert list2.get(1).equals(list3.get(1));
        assert list2.get(2).equals(list3.get(2));
        assert list3.size() == 3;
    }

    // testPartition
    @Test
    public void testGroupByNum() {
        List<Integer> list = ListUtils.newArrayList(1,2,3,4,5,6,7,8,9,10);
        List<List<Integer>> lists = ListUtils.groupByNum(list, 3);
        assetGroupByNum(lists);

        lists = ListUtils.partition(list, 3);
        assetGroupByNum(lists);

        Stream<List<Integer>> stream = ListUtils.partition(list.stream(), 3);
        lists = stream.collect(Collectors.toList());
        assetGroupByNum(lists);

        stream = ListUtils.groupByNum(list.stream(), 3);
        lists = stream.collect(Collectors.toList());
        assetGroupByNum(lists);

        // set的partition
        Set<Integer> set = new HashSet<>();
        for (int i = 1; i <= 10; i++) {
            set.add(i);
        }
        List<List<Integer>> partition = ListUtils.partition(set, 3);
        assert partition.size() == 4;
        assert partition.get(0).size() == 3;
    }

    private void assetGroupByNum(List<List<Integer>> lists) {
        assert lists.size() == 4;
        assert lists.get(0).size() == 3;
        assert lists.get(1).size() == 3;
        assert lists.get(2).size() == 3;
        assert lists.get(3).size() == 1;
        assert lists.get(0).get(0) == 1;
        assert lists.get(0).get(1) == 2;
        assert lists.get(0).get(2) == 3;
        assert lists.get(1).get(0) == 4;
        assert lists.get(1).get(1) == 5;
        assert lists.get(1).get(2) == 6;
    }

    @Test
    public void testRandom() {
        List<Integer> list = ListUtils.newArrayList();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        long start = System.currentTimeMillis();
        ListUtils.shuffle(list);
        long end = System.currentTimeMillis();
        System.out.println("cost:" + (end - start) + "ms");
        System.out.println(list);
    }

    @Test
    public void testFlatList() {
        List<List<Integer>> lists = new ArrayList<>();
        lists.add(ListUtils.newArrayList(1,2,3));
        lists.add(ListUtils.newArrayList(4,5,6));
        lists.add(ListUtils.newArrayList(7,8,9));

        List<Integer> flat = ListUtils.flat(lists);
        assert flat.size() == 9;
        assert NumberUtils.sum(flat).intValue() == 45;
    }

    @Test
    public void testMergeArray() {
        Object[] a = new Object[]{1,2,3};
        Object[] b = null;
        Object[] c = new Object[]{7,8,9};

        Object[] merge = ListUtils.concatArray(a, b, c);
        assert merge.length == 6;
        assert (int)merge[0] == 1;
        assert (int)merge[1] == 2;
        assert (int)merge[2] == 3;
        assert (int)merge[3] == 7;
        assert (int)merge[4] == 8;
        assert (int)merge[5] == 9;
    }

}
