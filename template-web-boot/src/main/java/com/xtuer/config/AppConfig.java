package com.xtuer.config;

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
    @Value("${app.dir.upload}")
    private String uploadDirectory;

    // 文件仓库目录
    @Value("${app.dir.repo}")
    private String repoDirectory;

    // 预览文件目录
    @Value("${app.dir.preview}")
    private String previewDirectory;

    // 身份认证 token 的有效期: 30 天
    @Value("${app.authTokenDuration}")
    private int authTokenDuration;

    @Value("${app.id}")
    private String appId;

    @Value("${app.key}")
    private String appKey;
}
