package edu.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 提供访问 Redis 缓存的功能，默认缓存时间为 1 个小时，也可以自己设置缓存时间
 */
@Service
public class RedisDao {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long DEFAULT_CACHE_DURATION_IN_SECONDS = 3600; // 默认缓存时间为 1 小时 (3600 秒)

    /**
     * 缓存优先读取 JavaBean，如果缓存中没有，则从数据库查询并保存到缓存
     *
     * @param redisKey Redis 中缓存的 key
     * @param clazz    实体类型
     * @param supplier 缓存失败时的数据提供器， supplier == null 时 return null
     * @param <T>      类型约束
     * @return 实体对象
     */
    public <T> T get(String redisKey, Class<T> clazz, Supplier<T> supplier) {
        return get(redisKey, clazz, supplier, DEFAULT_CACHE_DURATION_IN_SECONDS);
    }

    /**
     * 缓存优先读取 JavaBean，如果缓存中没有，则从数据库查询并保存到缓存
     *
     * @param redisKey       Redis 中缓存的 key
     * @param clazz          实体类型
     * @param supplier       缓存失败时的数据提供器， supplier == null 时 return null
     * @param timeoutSeconds 缓存超时时间，单位为秒
     * @param <T>            类型约束
     * @return 实体对象
     */
    public <T> T get(String redisKey, Class<T> clazz, Supplier<T> supplier, long timeoutSeconds) {
        T d = null;
        String json = redisTemplate.opsForValue().get(redisKey);

        if (json != null) {
            // 如果解析发生异常，有可能是 Redis 里的数据无效，故把其从 Redis 删除
            try {
                d = JSON.parseObject(json, clazz);
            } catch (Exception ex) {
                redisTemplate.delete(redisKey);
            }
        }

        if (d == null && supplier != null) {
            d = supplier.get();
            // 这里需要考虑，null 对象如果不放缓存，如果这个对象被大量访问，会导致缓存穿透，增加数据库的压力
            if (d != null) {
                redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(d), timeoutSeconds, TimeUnit.SECONDS);
            }
        }

        return d;
    }

    /**
     * 缓存优先读取 Collections Or Map，如果缓存中没有，则从数据库查询并保存到缓存
     *
     * @param redisKey      Redis 中缓存的 key
     * @param typeReference 反序列化集合时 FastJson 需要用 TypeReference 来指定类型，例如类型为 List<Demo>
     * @param supplier      缓存失败时的数据提供器，supplier == null 时 return null
     * @param <T>           类型约束
     * @return 实体对象
     */
    public <T> T get(String redisKey, TypeReference<T> typeReference, Supplier<T> supplier) {
        return get(redisKey, typeReference, supplier, DEFAULT_CACHE_DURATION_IN_SECONDS);
    }

    /**
     * 缓存优先读取 Collections Or Map，如果缓存中没有，则从数据库查询并保存到缓存
     *
     * @param redisKey       Redis 中缓存的 key
     * @param typeReference  反序列化集合时 FastJson 需要用 TypeReference 来指定类型，例如类型为 List<Demo>
     * @param supplier       缓存失败时的数据提供器，supplier == null 时 return null
     * @param timeoutSeconds 缓存超时时间，单位为秒
     * @param <T>            类型约束
     * @return 实体对象
     */
    public <T> T get(String redisKey, TypeReference<T> typeReference, Supplier<T> supplier, long timeoutSeconds) {
        T d = null;
        String json = redisTemplate.opsForValue().get(redisKey);

        if (json != null) {
            // 如果解析发生异常，有可能是 Redis 里的数据无效，故把其从 Redis 删除
            try {
                d = JSON.parseObject(json, typeReference);
            } catch (Exception ex) {
                redisTemplate.delete(redisKey);
            }
        }

        if (d == null && supplier != null) {
            d = supplier.get();
            // 这里需要考虑，null 对象如果不放缓存，如果这个对象被大量访问，会导致缓存穿透，增加数据库的压力
            if (d != null) {
                redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(d), timeoutSeconds, TimeUnit.SECONDS);
            }
        }

        return d;
    }

    /**
     * 使用 Redis Set 缓存数据，如果缓存没有命中，则使用提供器的数据并保存到缓存
     *
     * @param redisKey       Redis 中缓存的 key
     * @param supplier       缓存失败时的数据提供器
     * @param timeoutSeconds 缓存超时时间，单位为秒
     * @param <T>            类型约束
     * @return 对象集合
     */
    public <T> Set<T> getFromSet(String redisKey, Class<T> clazz, @Nullable Supplier<Set<T>> supplier, long timeoutSeconds) {
        BoundSetOperations<String, String> operations = redisTemplate.boundSetOps(redisKey);
        Set<String> members = operations.members(); // members 有可能返回 null

        if (members != null && !members.isEmpty()) {
            return members.stream().map(member -> JSON.parseObject(member, clazz)).collect(Collectors.toSet());
        } else if (supplier != null) {
            Set<T> newSet = supplier.get();

            if (newSet != null && !newSet.isEmpty()) {
                String[] array = newSet.stream().map(JSON::toJSONString).toArray(String[]::new);
                operations.add(array);
                operations.expire(timeoutSeconds, TimeUnit.SECONDS);

                return newSet;
            }
        }

        return Collections.emptySet();
    }

    /**
     * 指定的 value 是否在 Set values 中。
     * <p>
     * 如果未命中缓存，可能key对应的values未放入缓存或数据过期，考虑把数据放入缓存后再检索
     *
     * @param redisKey key
     * @param value    value
     * @return 是否命中缓存
     */
    public boolean isMember(String redisKey, String value) {
        Boolean is = redisTemplate.boundSetOps(redisKey).isMember(value); // isMember 有可能返回 null
        return is == null ? false : is;
    }

    /**
     * 删除 key 的缓存
     *
     * @param key 缓存的 key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
