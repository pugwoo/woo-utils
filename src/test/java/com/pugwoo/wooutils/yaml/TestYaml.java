package com.pugwoo.wooutils.yaml;

import com.pugwoo.wooutils.json.JSON;
import org.junit.Test;

import java.time.LocalDate;

public class TestYaml {

    public static class Student {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // 故意不提供setter，jackson只能通过setter来设置，所以没有setter就等于没有这个属性
//        public Integer getAge() {
//            return age;
//        }
//
//        public void setAge(Integer age) {
//            this.age = age;
//        }
    }

    public static class Info {

        private Student student;

        public Student getStudent() {
            return student;
        }

        public void setStudent(Student student) {
            this.student = student;
        }
    }

    @Test
    public void test() {
        String yaml = "student:\n"
                + "  name: nick\n"
                + "  age: 35\n";

        Info info = YAML.parse(yaml, Info.class);
        System.out.println(JSON.toJson(info));
        assert info.getStudent().getName().equals("nick");

        // 从指定节点开始解析
        Student student = YAML.parse(yaml, "/student", Student.class);
        System.out.println(JSON.toJson(student));
        assert student.getName().equals("nick");
    }

    @Test
    public void testLocalDate() {
        String yaml = "name: nick\n"
                + "birthday: 2022-11-12\n";
        WithLocalDate withLocalDate = YAML.parse(yaml, WithLocalDate.class);
        System.out.println(JSON.toJson(withLocalDate));
    }

    public static class WithLocalDate {
        private String name;
        private LocalDate birthday;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }
    }

}
