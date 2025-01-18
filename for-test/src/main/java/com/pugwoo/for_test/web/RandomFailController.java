package com.pugwoo.for_test.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RandomFailController {

    @RequestMapping("/random_fail")
    public String randomFail(String name, HttpServletResponse response) {
        if (Math.random() > 0.1) {
            response.setStatus(500);
            return "";
        } else {
            return "ok, your name is:" + name;
        }
    }

}
