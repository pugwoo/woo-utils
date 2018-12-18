package com.pugwoo.wooutils.collect;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestMerge {

    @Test
    public void test() {
        List<Student> studentList1 = new ArrayList<>();
        List<Student> studentList2 = new ArrayList<>();

        studentList1.add(new Student(1L, "one"));
        studentList1.add(new Student(3L, "three"));
        studentList1.add(new Student(5L, "five"));

        studentList2.add(new Student(2L, "two"));
        studentList2.add(new Student(4L, "four"));
        studentList2.add(new Student(5L, "five"));

        List<List<Student>> lists = new ArrayList<>();
        lists.add(studentList2);
        lists.add(studentList1);

        List<Student> merged = MergeSortUtils.merge(lists, true, 3L, 2, null);
        assert assertAsc(merged);
        assert merged.size() == 2;
        for(Student student : merged) {
            assert  student.getSeq().compareTo(3L) > 0;
        }

        merged = MergeSortUtils.merge(lists, true, null, 2, null);
        assert assertAsc(merged);
        assert merged.size() == 2;

        merged = MergeSortUtils.merge(lists, false, null, 2, null);
        assert assertDesc(merged);
        assert merged.size() == 2;

        merged = MergeSortUtils.merge(lists, false, 5L, 2, null);
        assert assertDesc(merged);
        assert merged.size() == 2;
        for(Student student : merged) {
            assert  student.getSeq().compareTo(5L) < 0;
        }

        merged = MergeSortUtils.merge(lists, true, null, 100, null);
        assert assertAsc(merged);
        assert merged.size() == 6;

        merged = MergeSortUtils.merge(lists, false, null, 100, null);
        assert assertDesc(merged);
        assert merged.size() == 6;
    }

    @Test
    public void test2() {
        List<Student> studentList1 = new ArrayList<>();
        List<Student> studentList2 = new ArrayList<>();

        studentList1.add(new Student(5L, "five"));
        studentList1.add(new Student(3L, "three"));
        studentList1.add(new Student(1L, "one"));

        studentList2.add(new Student(5L, "five"));
        studentList2.add(new Student(4L, "four"));
        studentList2.add(new Student(2L, "two"));

        List<List<Student>> lists = new ArrayList<>();
        lists.add(studentList1);
        lists.add(studentList2);

        List<Student> merged = MergeSortUtils.merge(lists, true, 3L, 2, null);
        assert assertAsc(merged);
        assert merged.size() == 2;
        for(Student student : merged) {
            assert  student.getSeq().compareTo(3L) > 0;
        }

        merged = MergeSortUtils.merge(lists, true, null, 2, null);
        assert assertAsc(merged);
        assert merged.size() == 2;

        merged = MergeSortUtils.merge(lists, false, null, 2, null);
        assert assertDesc(merged);
        assert merged.size() == 2;

        merged = MergeSortUtils.merge(lists, false, 5L, 2, null);
        assert assertDesc(merged);
        assert merged.size() == 2;
        for(Student student : merged) {
            assert  student.getSeq().compareTo(5L) < 0;
        }

        merged = MergeSortUtils.merge(lists, true, null, 100, null);
        assert assertAsc(merged);
        assert merged.size() == 6;

        merged = MergeSortUtils.merge(lists, false, null, 100, null);
        assert assertDesc(merged);
        assert merged.size() == 6;
    }

    private static <T extends MergeItem> boolean assertAsc(List<T> lists) {
        for(int i = 0; i < lists.size() - 2; i++) {
            if(lists.get(i).getSeq().compareTo(lists.get(i + 1).getSeq()) > 0) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void test3() {
        List<Student> studentList1 = new ArrayList<>();
        List<Student> studentList2 = new ArrayList<>();
        List<Student> studentList3 = new ArrayList<>();
        List<Student> studentList4 = new ArrayList<>();

        studentList1.add(new Student(1L, "one"));
        studentList2.add(new Student(2L, "two"));

        studentList3.add(new Student(3L, "three"));
        studentList3.add(new Student(3L, "three"));
        studentList3.add(new Student(3L, "three"));

        studentList4.add(new Student(4L, "four"));


        List<List<Student>> lists = new ArrayList<>();
        lists.add(studentList2);
        lists.add(studentList4);
        lists.add(studentList3);
        lists.add(studentList1);

        List<Student> merged = MergeSortUtils.merge(lists, true, null, 100, null);
        assert assertAsc(merged);
        assert merged.size() == 6;

        merged = MergeSortUtils.merge(lists, false, null, 100, null);
        assert assertDesc(merged);
        assert merged.size() == 6;
    }


    @Test
    public void testDup() {
        List<Student> studentList1 = new ArrayList<>();
        List<Student> studentList2 = new ArrayList<>();

        studentList1.add(new Student(1L, "one"));
        studentList1.add(new Student(3L, "three"));
        studentList1.add(new Student(5L, "five"));

        studentList2.add(new Student(2L, "two"));
        studentList2.add(new Student(4L, "four"));
        studentList2.add(new Student(5L, "five"));

        List<List<Student>> lists = new ArrayList<>();
        lists.add(studentList2);
        lists.add(studentList1);

        List<Student> merged = MergeSortUtils.merge(lists, true, 3L, 3, o -> o.getId());
        assert assertAsc(merged);
        assert merged.size() == 2; // 因为5去重了

        merged = MergeSortUtils.merge(lists, false, null, 6, o -> o.getId());
        assert assertDesc(merged);
        assert merged.size() == 5; // 因为5去重了
    }

    @Test
    public void testDup2() {
        List<Student> studentList1 = new ArrayList<>();
        List<Student> studentList2 = new ArrayList<>();

        studentList1.add(new Student(5L, "five"));
        studentList1.add(new Student(3L, "three"));
        studentList1.add(new Student(1L, "one"));

        studentList2.add(new Student(5L, "five"));
        studentList2.add(new Student(4L, "four"));
        studentList2.add(new Student(2L, "two"));

        List<List<Student>> lists = new ArrayList<>();
        lists.add(studentList2);
        lists.add(studentList1);

        List<Student> merged = MergeSortUtils.merge(lists, true, 3L, 3, o -> o.getId());
        assert assertAsc(merged);
        assert merged.size() == 2; // 因为5去重了

        merged = MergeSortUtils.merge(lists, false, null, 6, o -> o.getId());
        assert assertDesc(merged);
        assert merged.size() == 5; // 因为5去重了
    }

    private static <T extends MergeItem> boolean assertDesc(List<T> lists) {
        for(int i = 0; i < lists.size() - 2; i++) {
            if(lists.get(i).getSeq().compareTo(lists.get(i + 1).getSeq()) < 0) {
                return false;
            }
        }
        return true;
    }

    public static class Student implements MergeItem {
        private Long id;
        private String name;

        public Student(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Comparable getSeq() {
            return id;
        }
    }

}
