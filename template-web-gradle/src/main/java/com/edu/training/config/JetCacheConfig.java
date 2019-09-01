package com.edu.training.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.mzlion.core.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

/**
 * JetCache 的配置
 */
@Configuration
@EnableMethodCache(basePackages = "com.edu.training.service")
@EnableCreateCacheAnnotation
public class JetCacheConfig {
    @Autowired
    private AppConfig config;

    @Bean
    public Pool<Jedis> pool() {
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(config.getRedisMinIdle());
        pc.setMaxIdle(config.getRedisMaxIdle());
        pc.setMaxTotal(config.getRedisMaxTotal());

        String host = config.getRedisHost();
        int    port = config.getRedisPort();
        int timeout = config.getRedisTimeout();
        String pass = config.getRedisPassword();

        if (StringUtils.isEmpty(config.getRedisPassword())) {
            return new JedisPool(pc, host, port, timeout);
        } else {
            return new JedisPool(pc, host, port, timeout, pass);
        }
    }

    @Bean
    public SpringConfigProvider springConfigProvider() {
        return new SpringConfigProvider();
    }

    @Bean
    public GlobalCacheConfig config(SpringConfigProvider configProvider, Pool<Jedis> pool) {
        Map<String, CacheBuilder> localBuilders = new HashMap<>();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .expireAfterWrite(1000, TimeUnit.SECONDS) // 全局 expire，@Cached 能够指定自己的 expire
                .limit(3000);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        Map<String, CacheBuilder> remoteBuilders = new HashMap<>();
        RedisCacheBuilder remoteCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .expireAfterWrite(3600, TimeUnit.SECONDS) // 全局 expire，@Cached 能够指定自己的 expire
                .jedisPool(pool);
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteCacheBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setConfigProvider(configProvider);
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);
        globalCacheConfig.setAreaInCacheName(false);

        return globalCacheConfig;
    }
}
