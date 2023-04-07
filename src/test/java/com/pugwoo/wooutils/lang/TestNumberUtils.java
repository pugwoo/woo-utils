package com.pugwoo.wooutils.lang;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestNumberUtils {

    @Test
    public void parseIntTest() {
        assert Integer.valueOf(1).equals(NumberUtils.parseInt(1));
        assert Integer.valueOf(2).equals(NumberUtils.parseInt("2"));
        assert Integer.valueOf(3).equals(NumberUtils.parseInt(3L));
        
        assert null == NumberUtils.parseInt(1.0);
        assert null == NumberUtils.parseInt(1.2);
        assert null == NumberUtils.parseInt(new HashMap<String, Object>());
    }
    
    @Test
    public void parseLongTest() {
        assert Long.valueOf(1).equals(NumberUtils.parseLong(1));
        assert Long.valueOf(2).equals(NumberUtils.parseLong("2"));
        assert Long.valueOf(3).equals(NumberUtils.parseLong(3L));
        
        assert null == NumberUtils.parseLong(1.0);
        assert null == NumberUtils.parseLong(1.2);
        assert null == NumberUtils.parseLong(new HashMap<String, Object>());
    }
    
    @Test
    public void parseDoubleTest() {
        assert Double.valueOf(1).equals(NumberUtils.parseDouble(1));
        assert Double.valueOf(1.234).equals(NumberUtils.parseDouble("1.234"));
        assert Double.valueOf(1.234).equals(NumberUtils.parseDouble(1.234f));
        
        assert null == NumberUtils.parseDouble("aaa");
        assert null == NumberUtils.parseDouble(new HashMap<String, Object>());
    }
    
    @Test
    public void parseBigDecimalTest() {
        assert 0 == new BigDecimal("1.0001").compareTo(NumberUtils.parseBigDecimal("1.000100000"));
        assert 0 == new BigDecimal("1.0001").compareTo(NumberUtils.parseBigDecimal(1.0001));
        assert 0 == new BigDecimal("2").compareTo(NumberUtils.parseBigDecimal(2));
        
        assert null == NumberUtils.parseBigDecimal("1.0001.A");
        assert null == NumberUtils.parseDouble(new HashMap<String, Object>());
    }
    
    @Test
    public void roundUpTest() {
        assert "0.23".equals(NumberUtils.roundUp(0.234, 2));
        assert "0.24".equals(NumberUtils.roundUp(0.235, 2));
        assert "0.24".equals(NumberUtils.roundUp(0.236, 2));
    
        assert "0.234".equals(NumberUtils.roundUp(0.2344, 3));
        assert "0.235".equals(NumberUtils.roundUp(0.2345, 3));
        assert "0.235".equals(NumberUtils.roundUp(0.2346, 3));
    
        assert new BigDecimal("0.23").equals(NumberUtils.roundUp(new BigDecimal("0.234"), 2));
        assert new BigDecimal("0.24").equals(NumberUtils.roundUp(new BigDecimal("0.235"), 2));
        assert new BigDecimal("0.24").equals(NumberUtils.roundUp(new BigDecimal("0.236"), 2));
        
        assert new BigDecimal("0.234").equals(NumberUtils.roundUp(new BigDecimal("0.2344"), 3));
        assert new BigDecimal("0.235").equals(NumberUtils.roundUp(new BigDecimal("0.2345"), 3));
        assert new BigDecimal("0.235").equals(NumberUtils.roundUp(new BigDecimal("0.2346"), 3));
    
        assert new BigDecimal("0.23").equals(NumberUtils.roundUp(new BigDecimal(0.234), 2));
        // 注意 这个是double精度问题
        assert new BigDecimal("0.23").equals(NumberUtils.roundUp(new BigDecimal(0.235), 2));
        assert new BigDecimal("0.24").equals(NumberUtils.roundUp(new BigDecimal(0.236), 2));
    
        assert new BigDecimal("0.234").equals(NumberUtils.roundUp(new BigDecimal(0.2344), 3));
        // 注意 这个是double精度问题
        assert new BigDecimal("0.234").equals(NumberUtils.roundUp(new BigDecimal(0.2345), 3));
        assert new BigDecimal("0.235").equals(NumberUtils.roundUp(new BigDecimal(0.2346), 3));
    }
    
    @Test
    public void roundUpToDoubleTest() {
        assert 0.23 == NumberUtils.roundUpToDouble(0.234, 2);
        assert 0.24 == NumberUtils.roundUpToDouble(0.235, 2);
        assert 0.24 == NumberUtils.roundUpToDouble(0.236, 2);
        
        assert 0.234 == NumberUtils.roundUpToDouble(0.2344, 3);
        assert 0.235 == NumberUtils.roundUpToDouble(0.2345, 3);
        assert 0.235 == NumberUtils.roundUpToDouble(0.2346, 3);
    }
    
    @Test
    public void testSum() {

        List<String> numbers = new ArrayList<>();
        numbers.add("1.1");
        numbers.add("1.2");
        assert new BigDecimal("2.3").equals(NumberUtils.sum(numbers));
        assert new BigDecimal("2.3").equals(NumberUtils.sum(numbers, null));
        assert new BigDecimal("2.3").equals(NumberUtils.sum(numbers, o->o));
        assert new BigDecimal("2.3").equals(ListUtils.sum(numbers, o->o));
        
        List<Map<String, Object>> numberList = Stream.of(
                MapUtils.of("score", "1.1   "),
                MapUtils.of("score", "1.2   "),
                null,
                MapUtils.of("score", "A+"),
                MapUtils.of("name", "hello")
        ).collect(Collectors.toList());
        assert new BigDecimal("2.3").equals(NumberUtils.sum(numberList, o -> o.get("score")));
    }

    @Test
    public void testAvg() {
        List<String> numbers = new ArrayList<>();
        numbers.add("1.1");
        numbers.add("1.2");
        assert new BigDecimal("1.15").compareTo(NumberUtils.avg(numbers, 5)) == 0;
        assert new BigDecimal("1.15").compareTo(NumberUtils.avg(numbers, 5, null)) == 0;
        assert new BigDecimal("1.15").compareTo(NumberUtils.avg(numbers, 5, o -> o)) == 0;
        
        List<String> numbers2 = new ArrayList<>();
        numbers2.add("1.1");
        numbers2.add(null);
        numbers2.add("A+");
        assert new BigDecimal("0.36667").compareTo(NumberUtils.avg(numbers2, 5)) == 0;
        assert new BigDecimal("0.36667").compareTo(NumberUtils.avg(numbers2, 5, null)) == 0;
        assert new BigDecimal("0.36667").compareTo(NumberUtils.avg(numbers2,  5, o -> o)) == 0;
    
        List<Map<String, Object>> numberList = Stream.of(
                MapUtils.of("score", "1.1   "),
                MapUtils.of("score", "1.2   "),
                null,
                MapUtils.of("score", "A+"),
                MapUtils.of("name", "hello")
        ).collect(Collectors.toList());
        assert new BigDecimal("0.46").compareTo(NumberUtils.avg(numberList, 5, o -> o.get("score"))) == 0;
        assert new BigDecimal("0.46000").equals(NumberUtils.avg(numberList, 5, o -> o.get("score")));
        // 提供了错误的mapper，转换的数据无法转为BigDecimal，视为0
        assert new BigDecimal("0").compareTo(NumberUtils.avg(numberList, 5, o -> o)) == 0;
    }

    @Test
    public void testMinMax() {
        List<Integer> list = ListUtils.newArrayList(8,1,3,2,0,9,4);
        Integer min = NumberUtils.min(list, o -> o);
        Integer max = NumberUtils.max(list, o -> o);

        assert min == 0;
        assert max == 9;
    }

    @Test
    public void testMinMaxLocalDate() {
        List<LocalDate> localDates = new ArrayList<>();
        localDates.add(DateUtils.parseLocalDate("2019-01-01"));
        localDates.add(DateUtils.parseLocalDate("2019-01-02"));
        localDates.add(DateUtils.parseLocalDate("2019-01-03"));

        LocalDate min = NumberUtils.min(localDates, o -> o);
        LocalDate max = NumberUtils.max(localDates, o -> o);
        assert DateUtils.format(min, "yyyy-MM-dd").equals("2019-01-01");
        assert DateUtils.format(max, "yyyy-MM-dd").equals("2019-01-03");
    }

}
