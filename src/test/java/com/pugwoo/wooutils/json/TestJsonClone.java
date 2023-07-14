package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

public class TestJsonClone {

    @Test
    public void cloneTest() {
        System.out.println("\n================ clone实例-不支持泛型");
        Map<String, Date> map = new HashMap<>();
        map.put("date", new Date());
        // clone 不支持泛型
        Map<String, Date> mapClone = JSON.clone(map);

        System.out.println("     map: " + map);
        System.out.println("mapClone: " + mapClone);
    }

    @Test
    public void cloneReferenceTest() {
        System.out.println("\n================ clone实例-类型引用");
        Map<String, Date> map = new HashMap<>();
        map.put("date", new Date());

        Map<String, Date> mapClone = JSON.clone(map, new TypeReference<Map<String, Date>>() {});

        System.out.println("     map: " + map);
        System.out.println("mapClone: " + mapClone);

        assert mapClone.get("date").getClass() == Date.class;

        mapClone = JSON.clone(map, String.class, Date.class);

        System.out.println("     map: " + map);
        System.out.println("mapClone: " + mapClone);

        assert mapClone.get("date").getClass() == Date.class;
    }

    @Test
    public void cloneReference2Test() {
        Map<String, Date> map = new HashMap<>();
        map.put("date", new Date());

        Map<String, Map<String, Date>> listItem = new HashMap<>();
        listItem.put("mapmap", map);

        List<Map<String, Map<String, Date>>> list = new ArrayList<>();
        list.add(listItem);

        TypeReference<List<Map<String, Map<String, Date>>>> typeReference =
                new TypeReference<List<Map<String, Map<String, Date>>>>() {};
        String listJson = JSON.toJson(list);

        List<Map<String, Map<String, Date>>> listClone = JSON.clone(list, typeReference);

        System.out.println("list source: " + list);
        System.out.println("list json  : " + listJson);
        System.out.println("list clone : " + listClone);

        assert listClone.get(0).get("mapmap").get("date").getClass() == Date.class;

        // 说明：这种方式不支持嵌套泛型，所以解析不了上面的类型
        listClone = JSON.clone(list, Map.class);
        System.out.println("list source: " + list);
        System.out.println("list json  : " + listJson);
        System.out.println("list clone : " + listClone);

        // assert listClone.get(0).get("mapmap").get("date").getClass() == Date.class; // 报错
    }

    public static class DateDTO {
        private Date birth;

        public Date getBirth() {
            return birth;
        }
        public void setBirth(Date birth) {
            this.birth = birth;
        }
    }

    @Test
    public void testTimeMs() {
        DateDTO dateDTO = new DateDTO();
        dateDTO.setBirth(new Date());

        DateDTO cloned = JSON.clone(dateDTO);
        assert dateDTO.getBirth().getTime() == cloned.getBirth().getTime();

        List<DateDTO> list = new ArrayList<>();
        list.add(dateDTO);

        List<DateDTO> clonedList = JSON.clone(list, DateDTO.class);
        assert clonedList.get(0).getBirth().getTime() == dateDTO.getBirth().getTime();

    }

    public static class DateNumDTO {
        String date;
        BigDecimal value;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    @Test
    public void testSubListClone() {
        List<DateNumDTO> data = new ArrayList<>();

        DateNumDTO d1 = new DateNumDTO();
        d1.setDate("2020-01-01");
        d1.setValue(new BigDecimal(1));

        data.add(d1);

        DateNumDTO d2 = new DateNumDTO();
        d2.setDate("2020-01-02");
        d2.setValue(new BigDecimal(2));

        data.add(d2);

        DateNumDTO d3 = new DateNumDTO();
        d3.setDate("2020-01-03");
        d3.setValue(new BigDecimal(3));
        data.add(d3);

        List<DateNumDTO> dateNumDTOS = data.subList(1, 2);

        List<DateNumDTO> clone = JSON.clone(dateNumDTOS, DateNumDTO.class);
        assert clone.size() == 1;
        assert clone.get(0).getDate().equals("2020-01-02");
        assert clone.get(0).getValue().compareTo(new BigDecimal(2)) == 0;
    }
}
