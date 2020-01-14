package com.xtuer.service;

import com.alicp.jetcache.anno.Cached;
import com.xtuer.bean.User;
import com.xtuer.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 性能测试的服务: 针对不同的服务进行了独立测试，便于发现问题
 */
@Service
public class PerformanceTestService {
    private static final Random RAND = new Random();

    @Autowired
    private PerformanceTestService self;

    @Autowired
    private UserMapper userMapper;

    // 测试机器的运算性能
    public String calculate() {
        int sum = 1 + 2 * 3 / 4 - 5;
        return "" + sum;
    }

    // 测试数据库的查询
    public User db() {
        // 随机生成 1 万个不同的 ID 进行查询，避免数据库缓存相同条件的查询
        long id = RAND.nextInt(10_000);
        return userMapper.findUserById(id);
    }

    // 测试 Redis 的读性能
    public String redisRead() {
        return self.redis(RAND.nextInt(1000)); // 缓存最多 1000 个数据
    }

    // 测试 Redis 的写性能
    public String redisWrite() {
        return self.redis(RAND.nextInt(100_000)); // 缓存最多 10 万个数据
    }

    // 测试 Redis 的处理能力
    @Cached(name = "cache-test:", key = "'pt-name-' + #ns")
    public String redis(int ns) {
        return "Performance Test: " + ns;
    }
}
