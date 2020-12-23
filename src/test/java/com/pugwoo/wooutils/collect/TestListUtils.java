package com.pugwoo.wooutils.collect;

import org.junit.Test;

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

}
