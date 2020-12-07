package com.pugwoo.wooutils.lang;

import com.pugwoo.wooutils.collect.ListUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestNumberUtils {

    @Test
    public void testSum() {

        List<String> numbers = new ArrayList<>();
        numbers.add("1.1");
        numbers.add("1.2");

        assert new BigDecimal("2.3").equals(NumberUtils.sum(numbers, o->o));
        assert new BigDecimal("2.3").equals(ListUtils.sum(numbers, o->o));

    }

}
