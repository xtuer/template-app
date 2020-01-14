package com.xtuer.controller;

import com.xtuer.bean.Result;
import com.xtuer.bean.User;
import com.xtuer.service.PerformanceTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 压力测试的控制器
 */
@Controller
public class PerformanceTestController {
    @Autowired
    private PerformanceTestService performanceTestService;

    /**
     * 测试机器的运算能力
     */
    @GetMapping("/api/pt/calculate")
    @ResponseBody
    public String calculate() {
        return performanceTestService.calculate();
    }

    /**
     * 测试数据库的查询性能
     */
    @GetMapping("/api/pt/db")
    @ResponseBody
    public Result<User> db() {
        return Result.ok(performanceTestService.db());
    }

    /**
     * 测试 Redis 的读性能
     */
    @GetMapping("/api/pt/redis/read")
    @ResponseBody
    public String redisRead() {
        return performanceTestService.redisRead();
    }

    /**
     * 测试 Redis 的读性能
     */
    @GetMapping("/api/pt/redis/write")
    @ResponseBody
    public String redisWrite() {
        return performanceTestService.redisWrite();
    }
}
