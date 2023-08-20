package com.pugwoo.wooutils.algorithm;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestDynamicBaseNumber {

    @Test
    public void test() {
        int[] base = new int[]{10, 10, 10};
        int[] number = DynamicBaseNumber.getZero(base);
        List<Integer> list = new ArrayList<>();
        while(!DynamicBaseNumber.isMax(base, number)) {
            DynamicBaseNumber.increment(base, number);
            list.add(number[2] * 100 + number[1] * 10 + number[0]);
        }

        // 检查一下list里有0到999
        for (int i = 1; i < 1000; i++) {
            assert list.contains(i);
        }

        // decrement测试
        list = new ArrayList<>();
        while(!DynamicBaseNumber.isZero(base, number)) {
            list.add(number[2] * 100 + number[1] * 10 + number[0]);
            DynamicBaseNumber.decrement(base, number);
        }

        for (int i = 1; i < 1000; i++) {
            assert list.contains(i);
        }
    }

}
