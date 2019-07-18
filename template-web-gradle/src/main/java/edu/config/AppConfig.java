package edu.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 应用的配置，系统中使用的配置都使用一个变量保存，方便访问以及修改
 */
@Configuration
@Getter
@Setter
public class AppConfig {
    // 上传文件的临时文件夹
    @Value("${uploadDirectory}")
    private String uploadDirectory;

    // 文件仓库目录
    @Value("${repoDirectory}")
    private String repoDirectory;

    // 预览文件目录
    @Value("${previewDirectory}")
    private String previewDirectory;

    // 身份认证 token 的有效期: 30 天
    @Value("${authTokenDuration}")
    private int authTokenDuration;

    @Value("${appId}")
    private String appId;

    @Value("${appKey}")
    private String appKey;

    // Redis 配置
    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Value("${redis.timeout}")
    private int redisTimeout;

    @Value("${redis.password}")
    private String redisPassword;

    @Value("${redis.minIdle}")
    private int redisMinIdle;

    @Value("${redis.maxIdle}")
    private int redisMaxIdle;

    @Value("${redis.maxTotal}")
    private int redisMaxTotal;
}
