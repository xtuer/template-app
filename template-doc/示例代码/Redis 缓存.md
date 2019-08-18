[JetCache](https://github.com/alibaba/jetcache/wiki) 是一个基于 Java 的缓存系统封装，提供统一的API和注解来简化缓存的使用。 JetCache 提供了比 SpringCache 更加强大的注解，可以原生的支持 TTL、两级缓存、分布式自动刷新，还提供了 `Cache` 接口用于手工缓存操作。 当前有四个实现，`RedisCache`、`TairCache` (此部分未在 github 开源)、`CaffeineCache` (in memory) 和一个简易的`LinkedHashMapCache` (in memory)，要添加新的实现也是非常简单的。

网上很多文章介绍 JetCache 的文章包括官方文档主要是基于 Spring Boot 的，也介绍了[未使用 SpringBoot 的配置方式](https://github.com/alibaba/jetcache/wiki/GettingStarted_CN#未使用springboot的配置方式)，但是估计很多同学还是不明白怎么在传统的 Spring MVC 的 Web 项目里使用 JetCache 吧，毕竟不是所有 Web 项目都使用 Spring Boot，接下来就一步一步的介绍使用的方法。

使用缓存示例:

```java
@Service
public class HelloService {
    @Cached(name = "user.", key = "#userId", expire = 600)
    public String getUsernameById(long userId) {
        System.out.println("Fetch username from DB");
        return "Bob";
    }

    @CacheInvalidate(name = "user.", key = "#userId")
    public void removeUsername(long userId) {
        System.out.println("Remove user from Redis");
    }
}
```

更详细信息请参考 https://qtdebug.com/spring-jetcache/。

