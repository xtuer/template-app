package com.xtuer.controller;

import com.xtuer.bean.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ZooController {
    /**
     * 把字符串自动转为日期
     *
     * 网址:
     *      http://localhost:8080/api/demo/string2date?date=2020-01-01
     *      http://localhost:8080/api/demo/string2date?date=2020-10-04%2012:10:00
     *
     * 参数: date [String]: 字符串格式的日期
     */
    @GetMapping("/api/demo/string2date")
    public Date convertStringToDate(@RequestParam Date date) {
        return date;
    }

    @GetMapping("/api/demo/exception")
    public String exception() {
        throw new RuntimeException();
    }

    /**
     * 测试 POST 请求中有中文 (默认应该使用了 UTF-8)
     *
     * 网址:
     *      http://localhost:8080/api/demo/encoding
     * 参数: name [String]: 中文字符串
     */
    @PostMapping("/api/demo/encoding")
    public Result<String> encoding(@RequestParam String name) {
        return Result.ok(name);
    }
}
