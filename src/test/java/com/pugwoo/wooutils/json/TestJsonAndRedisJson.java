package com.pugwoo.wooutils.json;

import com.pugwoo.wooutils.redis.impl.JsonRedisObjectConverter;
import org.junit.Test;

import java.util.Date;

/**
 * 测试json和redis json的兼容性，主要是时间戳
 */
public class TestJsonAndRedisJson {

    public static class Student {
        private Date date;

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @Test
    public void test() {

        Student student = new Student();
        student.setDate(new Date());

        String json2 = new JsonRedisObjectConverter().convertToString(student);

        Student student3 = JSON.parse(json2, Student.class);
        Student student4 = new JsonRedisObjectConverter().convertToObject(json2, Student.class);

        assert student.getDate().equals(student3.getDate());
        assert student.getDate().equals(student4.getDate());

    }


}
