package com.pugwoo.wooutils.collect;

import com.pugwoo.wooutils.json.JSON;
import com.pugwoo.wooutils.lang.NumberUtils;
import org.junit.jupiter.api.Test;

import java.util.*;
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

    public static class OneDTO {
        private String name;
        private Integer one;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getOne() {
            return one;
        }

        public void setOne(Integer one) {
            this.one = one;
        }
    }

    public static class TwoDTO {
        private String name;
        private Integer two;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getTwo() {
            return two;
        }

        public void setTwo(Integer two) {
            this.two = two;
        }
    }

    public static class OneAndTwoDTO {
        private String name;
        private Integer one;
        private Integer two;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getOne() {
            return one;
        }

        public void setOne(Integer one) {
            this.one = one;
        }

        public Integer getTwo() {
            return two;
        }

        public void setTwo(Integer two) {
            this.two = two;
        }
    }

    @Test
    public void testMerge() {
        List<OneDTO> oneDTOS = new ArrayList<>();
        OneDTO oneDTO = new OneDTO();
        oneDTO.setName("a");
        oneDTO.setOne(1);
        oneDTOS.add(oneDTO);

        oneDTO = new OneDTO();
        oneDTO.setName("b");
        oneDTO.setOne(2);
        oneDTOS.add(oneDTO);

        List<TwoDTO> twoDTOS = new ArrayList<>();
        TwoDTO twoDTO = new TwoDTO();
        twoDTO.setName("a");
        twoDTO.setTwo(3);
        twoDTOS.add(twoDTO);

        twoDTO = new TwoDTO();
        twoDTO.setName("b");
        twoDTO.setTwo(4);
        twoDTOS.add(twoDTO);

        List<OneAndTwoDTO> oneAndTwoDTOS = ListUtils.merge(oneDTOS, twoDTOS,
                o -> o.getName(), o -> o.getName(),
                (listOne, listTwo) -> {
                    String name = ListUtils.isEmpty(listOne) ? listTwo.get(0).getName() : listOne.get(0).getName();
                    OneAndTwoDTO oneAndTwoDTO = new OneAndTwoDTO();
                    oneAndTwoDTO.setName(name);
                    oneAndTwoDTO.setOne(ListUtils.isEmpty(listOne) ? null : listOne.get(0).getOne());
                    oneAndTwoDTO.setTwo(ListUtils.isEmpty(listTwo) ? null : listTwo.get(0).getTwo());
                    return oneAndTwoDTO;
                });

        System.out.println(JSON.toJson(oneAndTwoDTOS));
        assert oneAndTwoDTOS.size() == 2;
        assert oneAndTwoDTOS.get(0).getName().equals("a");
        assert oneAndTwoDTOS.get(0).getOne() == 1;
        assert oneAndTwoDTOS.get(0).getTwo() == 3;
        assert oneAndTwoDTOS.get(1).getName().equals("b");
        assert oneAndTwoDTOS.get(1).getOne() == 2;
        assert oneAndTwoDTOS.get(1).getTwo() == 4;
    }

    @Test
    public void testConcat() {
        List<Integer> list1 = ListUtils.newArrayList(1,2,3);
        List<Integer> list2 = ListUtils.newArrayList(4,5,6);
        List<Integer> list3 = ListUtils.newArrayList(7,8,9);

        Stream<Integer> stream = ListUtils.concat(list1, list2, list3);
        List<Integer> list = ListUtils.toList(stream);
        assert list.size() == 9;
        assert list.get(0) == 1;
        assert list.get(1) == 2;
        assert list.get(2) == 3;
        assert list.get(3) == 4;
        assert list.get(4) == 5;
        assert list.get(5) == 6;
        assert list.get(6) == 7;
        assert list.get(7) == 8;
        assert list.get(8) == 9;
    }

    @Test
    public void testDup() {
        List<Integer> list1 = ListUtils.newList(1,2,3,4);
        List<Integer> list2 = ListUtils.newList(1,2,3,2);

        assert !ListUtils.hasDuplicate(list1, o -> o);
        assert ListUtils.hasDuplicate(list2, o -> o);

        assert ListUtils.getDuplicates(list1, o -> o).size() == 0;
        assert ListUtils.getDuplicates(list2, o -> o).size() == 1;
        assert ListUtils.getDuplicates(list2, o -> o).get(2) == 2;

        assert !ListUtils.hasDuplicateNotBlank(ListUtils.newList("", "", ""));
        assert ListUtils.hasDuplicateNotBlank(ListUtils.newList("", "a", "", "a"));

        Map<String, Integer> map2 = ListUtils.getDuplicatesNotBlank(ListUtils.newList("", "", ""));
        assert map2.size() == 0;

        Map<String, Integer> map1 = ListUtils.getDuplicatesNotBlank(ListUtils.newList("", "a", "", "a"));
        assert map1.size() == 1;
        assert map1.get("a") == 2;
    }

    @Test
    public void testReplace() {
        List<Integer> list1 = ListUtils.newList(1,2,3,4,3);
        assert ListUtils.replaceAll(list1, 3, 5);
        assert list1.get(2).equals(5);
        assert list1.get(4).equals(5);
    }

    @Test
    public void testToMapSet(){
        List<OneDTO> oneDTOS = new ArrayList<>();
        OneDTO oneDTO = new OneDTO();
        oneDTO.setName("a");
        oneDTO.setOne(1);
        oneDTOS.add(oneDTO);


        oneDTO = new OneDTO();
        oneDTO.setName("a");
        oneDTO.setOne(1);
        oneDTOS.add(oneDTO);

        oneDTO = new OneDTO();
        oneDTO.setName("a");
        oneDTO.setOne(2);
        oneDTOS.add(oneDTO);

        oneDTO = new OneDTO();
        oneDTO.setName("b");
        oneDTO.setOne(3);
        oneDTOS.add(oneDTO);

        Map<String, Set<Integer>> rzt = ListUtils.toMapSet(oneDTOS, o -> o.getName(), o -> o.getOne());

        assert rzt.size() == 2;
        assert rzt.get("a").size() == 2;
        assert rzt.get("a").contains(1);
        assert rzt.get("a").contains(2);
        assert rzt.get("b").size() == 1;
        assert rzt.get("b").contains(3);

    }

    @Test
    public void testDistinct() {
        List<OneDTO> oneDTOS = new ArrayList<>();
        OneDTO oneDTO = new OneDTO();
        oneDTO.setName("a");
        oneDTO.setOne(1);
        oneDTOS.add(oneDTO);

        oneDTO = new OneDTO();
        oneDTO.setName("a");
        oneDTO.setOne(1);
        oneDTOS.add(oneDTO);

        oneDTO = new OneDTO();
        oneDTO.setName("a");
        oneDTO.setOne(2);
        oneDTOS.add(oneDTO);

        oneDTO = new OneDTO();
        oneDTO.setName("b");
        oneDTO.setOne(3);
        oneDTOS.add(oneDTO);

        List<OneDTO> distinct = ListUtils.distinct(oneDTOS, o -> o.getName());
        assert distinct.size() == 2;
    }

}
