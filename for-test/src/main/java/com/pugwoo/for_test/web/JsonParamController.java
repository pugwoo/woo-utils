package com.pugwoo.for_test.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonParamController {

    public static class Param {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @PostMapping("/json_param")
    public String testJsonParam(@RequestBody Param param) {
        return "name=" + param.getName() + ", age=" + param.getAge();
    }

}
