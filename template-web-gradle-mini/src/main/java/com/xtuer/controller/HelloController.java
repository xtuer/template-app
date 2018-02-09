package com.xtuer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HelloController {
    @GetMapping("/")
    @ResponseBody
    public String index() {
        return "Home page";
    }

    /**
     * http://localhost:8080/page/hello
     */
    @GetMapping("/page/hello")
    public String hello(ModelMap model) {
        model.put("name", "Biao");

        return "hello.html";
    }

    /**
     * http://localhost:8080/api/json
     */
    @GetMapping("/api/json")
    @ResponseBody
    public Object json() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "Biao");
        map.put("age", "23");

        return map;
    }
}
