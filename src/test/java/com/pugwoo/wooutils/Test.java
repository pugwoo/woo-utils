package com.pugwoo.wooutils;

import com.pugwoo.wooutils.json.JSON;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Test {

    public static class A {
        private String a;
        private LocalDate ld;
        private LocalDateTime ldt;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public LocalDate getLd() {
            return ld;
        }

        public void setLd(LocalDate ld) {
            this.ld = ld;
        }

        public LocalDateTime getLdt() {
            return ldt;
        }

        public void setLdt(LocalDateTime ldt) {
            this.ldt = ldt;
        }
    }

    public static void main(String[] args) {

//        LocalDateTime localDateTime = DateUtils.parseLocalDateTime("2021-02-03T04:05:06");
//        System.out.println(localDateTime);

        String json = "{\"a\":\"name\",\"ld\":\"2022-03-04\",\"ldt\":\"2021-02-03 04:05:06\"}";
        System.out.println(json);

        A a = JSON.parse(json, A.class);

        System.out.println(JSON.toJson(a));

    }

}