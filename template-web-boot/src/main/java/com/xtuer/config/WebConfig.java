package com.xtuer.config;

import com.xtuer.converter.DateConverter;
import com.xtuer.converter.JacksonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
public class WebConfig {
    /**
     * 启用 Jackson 为 HttpMessageConverter，转换对象为 JSON 字符串
     */
    @Bean
    public HttpMessageConverters jacksonHttpMessageConverters() {
        JacksonHttpMessageConverter jacksonHttpMessageConverter = new JacksonHttpMessageConverter();
        return new HttpMessageConverters(jacksonHttpMessageConverter);
    }

    /**
     * 创建把字符串转日期对象的转换器
     */
    @Bean
    public DateConverter dateConverter() {
        return new DateConverter();
    }

    /**
     * 浏览器的 form 不支持 put, delete 等 method, 由该 filter 将 /blog?_method=delete 转换为标准的 http delete 方法。
     * 另一个好处是 PUT, PATCH 等请求可以使用 content-type: application/x-www-form-urlencoded，用 key=value 的形式提交请求，
     * Controller 中接收参数简单一些
     *
     * @return 返回 Servlet 的 Filter HiddenHttpMethodFilter
     */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
