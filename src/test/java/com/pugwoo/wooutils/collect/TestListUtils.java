package com.pugwoo.wooutils.collect;

import com.pugwoo.wooutils.lang.NumberUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestListUtils {

    @Test
    public void testNewArray() {

        List<Integer> list = ListUtils.newArrayList(1, 2, 3);
        assert list.size() == 3;
        assert list.get(0).equals(1);
        assert list.get(1).equals(2);
        assert list.get(2).equals(3);

        Set<Integer> sets = ListUtils.toSet(list, o -> o);
        List<Integer> list2 = ListUtils.toList(sets);

        assert list2.size() == 3;
        assert list2.get(0).equals(1);
        assert list2.get(1).equals(2);
        assert list2.get(2).equals(3);

    }

    @Test
    public void testGroupByNum() {
        List<Integer> list = ListUtils.newArrayList(1,2,3,4,5,6,7,8,9,10);
        List<List<Integer>> lists = ListUtils.groupByNum(list, 3);
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

}
