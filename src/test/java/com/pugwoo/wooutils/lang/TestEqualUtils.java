package com.pugwoo.wooutils.lang;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.collect.MapUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class TestEqualUtils {

    @Test
    public void testBasic() {
        EqualUtils equalUtils = new EqualUtils();

        assert equalUtils.isEqual(1,1);
        assert !equalUtils.isEqual(1,2);
        assert !equalUtils.isEqual(null, 3);
        assert equalUtils.isEqual(null, null);
        assert equalUtils.isEqual("hello", "hello");
        assert !equalUtils.isEqual("hello", "world");

        int[] a = new int[] {1,2,3};
        int[] b = new int[] {1,2,3};
        assert equalUtils.isEqual(a, b);

        a = new int[] {1,2,3};
        b = new int[] {1,2,4};
        assert !equalUtils.isEqual(a, b);

        byte[] c = "hello".getBytes();
        byte[] d = "hello".getBytes();
        assert equalUtils.isEqual(c, d);

        assert !equalUtils.isEqual(a, c);
    }

    @Test
    public void testList() {
        EqualUtils equalUtils = new EqualUtils();

        List<Integer> list1 = ListUtils.newArrayList(1,2,3);
        List<Integer> list2 = ListUtils.newArrayList(3,2,1);

        assert !equalUtils.isEqual(list1, list2);

        equalUtils.ignoreListOrder(true);
        assert equalUtils.isEqual(list1, list2);

        list2.add(4);
        assert !equalUtils.isEqual(list1, list2);
        list1.add(5);
        assert !equalUtils.isEqual(list1, list2);
    }

    @Test
    public void testMapAndSet() {
        EqualUtils equalUtils = new EqualUtils();

        Map<String, Object> map1 = MapUtils.of("one", 1, "two", 2);
        Map<String, Object> map2 = MapUtils.of("one", 1, "two", 2);

        assert equalUtils.isEqual(map1, map2);

        map2.put("three", 3);
        assert !equalUtils.isEqual(map1, map2);

        map1.put("three", 33);
        assert !equalUtils.isEqual(map1, map2);

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();

        set1.add("hello");
        set2.add("world");

        assert !equalUtils.isEqual(set1, set2);
        set2.add("hello");
        assert !equalUtils.isEqual(set1, set2);
        set1.add("world");
        assert equalUtils.isEqual(set1, set2);
    }

    public static class StudentDO {
        private Long id;
        private String name;
        private Date birth;

        public StudentDO() {}

        public StudentDO(Long id, String name, Date birth) {
            this.id = id;
            this.name = name;
            this.birth = birth;
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

        public Date getBirth() {
            return birth;
        }

        public void setBirth(Date birth) {
            this.birth = birth;
        }
    }

    @Test
    public void testObjectEqual() {
        Date now = new Date();
        StudentDO student1 = new StudentDO(1L, "nick", now);
        StudentDO student2 = new StudentDO(1L, "nick", now);

        EqualUtils equalUtils = new EqualUtils();
        assert equalUtils.isEqual(student1, student2);

        StudentDO[] arr1 = new StudentDO[2];
        StudentDO[] arr2 = new StudentDO[2];
        arr1[0] = student1;
        arr1[1] = student2;
        arr2[0] = student1;
        arr2[1] = student2;
        assert equalUtils.isEqual(arr1, arr2);

        student2.setName("karen");
        assert !equalUtils.isEqual(student1, student2);

        arr2[0] = student2;
        arr2[1] = student1;
        assert !equalUtils.isEqual(arr1, arr2);

        equalUtils.ignoreAttr("name");
        equalUtils.ignoreAttr("name"); // 可以重复写
        assert equalUtils.isEqual(student1, student2);
    }

    @Test
    public void testOthers() {
        byte[] b1 = new byte[10];
        InputStream in1 = new ByteArrayInputStream(b1);
        byte[] b2 = new byte[10];
        InputStream in2 = new ByteArrayInputStream(b2);
        assert new EqualUtils().isEqual(in1, in2); // 输入或输出流不比对
    }

    public static class SchoolDO {
        private String name;
        private List<StudentDO> students = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<StudentDO> getStudents() {
            return students;
        }

        public void setStudents(List<StudentDO> students) {
            this.students = students;
        }
    }

    @Test
    public void testNestedCompare() {
        Date now = new Date();
        StudentDO student1 = new StudentDO(1L, "nick", now);
        StudentDO student2 = new StudentDO(2L, "karen", now);

        SchoolDO school1 = new SchoolDO();
        school1.setName("sysu");
        school1.getStudents().add(student1);
        school1.getStudents().add(student2);

        SchoolDO school2 = new SchoolDO();
        school2.setName("sysu");
        school2.getStudents().add(student2);
        school2.getStudents().add(student1);

        EqualUtils equalUtils = new EqualUtils();
        assert !equalUtils.isEqual(school1, school2);

        equalUtils.ignoreListOrder(true);
        assert equalUtils.isEqual(school1, school2);
    }
}
