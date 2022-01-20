package com.pugwoo.wooutils.collect;

import com.pugwoo.wooutils.json.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {

    public static void main(String[] args) {

        List<Map<String, Object>> list = new ArrayList<>();

        // 按名字正序
        SortingField<Map<String, Object>, Comparable<?>> byNameAsc =
                new SortingField<Map<String, Object>, Comparable<?>>(SortingOrderEnum.ASC) {
            @Override
            public Comparable<?> apply(Map<String, Object> input) {
                return (String) input.get("name");
            }
        };

        // 按id逆序
        SortingField<Map<String, Object>, Comparable<?>> byIdDesc =
                new SortingField<Map<String, Object>, Comparable<?>>(SortingOrderEnum.DESC) {
                    @Override
                    public Comparable<?> apply(Map<String, Object> input) {
                        return (Comparable<?>) input.get("id");
                    }
                };

        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "a");
        map1.put("id", 3);
        list.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "a");
        map2.put("id", 4);
        list.add(map2);

        SortingUtils.sort(list, byNameAsc, byIdDesc); // 可以写很多个，不限制

        System.out.println(JSON.toJson(list));

    }

}
