使用 Snowflake 算法生成全局唯一的 ID，可以保证 1024(32*32) 台服务器生成的 ID 在 70 年内是唯一的，理论请参考 [分布式 ID 生成算法 Snowflake](http://qtdebug.com/java-snowflake/)

![](http://qtdebug.com/img/java/snowflake.png)

## 配置 IdWorker

提供了 2 中创建 IdWorker 的方式:

* 方式一: `按序号管理服务器`，配置 workerId，范围是 [0, 1023]，当不同的服务器使用不同的 workerId 时能够保证生成的 ID 是**分布式唯一**的
* 方式二: `按数据中心管理服务器`，配置 datacenterId 和 workerId，他们的范围都是 [0, 31]，当不同的服务器使用不同的 datacenterId 和 workerId 时能够保证生成的 ID 是**分布式唯一**的

## 生成 ID 有 2 步

1. 注入 idWorker

   ```java
   @Autowired
   private IdWorker idWorker;
   ```

2. 生成 ID

   ```java
   Long id = idWorker.nextId();
   ```

   ​

