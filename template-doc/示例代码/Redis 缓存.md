在代码中使用 Redis 缓存需要借助类 `StringRedisTemplate` 或者  `RedisDao`。RedisDao 故名思义就是要和数据库一起使用的: 访问的时候，先去 Redis 查找是否已经缓存:

* 如果已经缓存则直接从 Redis 读取，不再查询数据库
* 如果没有缓存在 Redis 中，则先从数据库查询，并且缓存到数据库

## RedisDao

使用 RedisDao 编写代码时如下即可:

1. 注入 RedisDao

   ```java
   @Autowired
   private RedisDao redis;
   ```

2. 访问单个 Bean，提供 Class 和 mapper 查找数据的方法

   ```java
   String redisKey = "demo_" + id; // 对象在 Redis 中的 key
   Demo d = redis.get(redisKey, Demo.class, () -> demoMapper.findDemoById(id));
   ```

3. 访问 List, Map，提供 TypeReference 和 mapper 查找数据的方法

   ```java
   List<Demo> demos = redis.get("demos", new TypeReference<List<Demo>>(){}, () -> demoMapper.allDemos());
   ```

如果对实现原理感兴趣，可参考 <http://qtdebug.com/spring-web-redis/>

> 注意: 不要什么数据都用缓存，能够使用缓存的数据要很少变化才行，并且更新的时候需要把响应缓存中的数据更新或者删除掉，否则数据库更新了，缓存里还是旧的数据。

## StringRedisTemplate

使用 StringRedisTemplate 编写代码时如下即可:

1. 注入 StringRedisTemplate

   ```java
   @Autowired
   private StringRedisTemplate redisTemplate;
   ```

2. 使用 StringRedisTemplate 的函数，具体请查看文档

   ```java
   redisTemplate.opsForHash()
   redisTemplate.opsForSet()
   redisTemplate.opsForZSet()
   redisTemplate.opsForList()
   redisTemplate.opsForValue()
   ------------------------------------
   redisTemplate.opsForValue().get()
   redisTemplate.opsForValue().set()
   redisTemplate.delete()
   ```

   ​

