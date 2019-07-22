package edu.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;

import java.math.BigInteger;

/**
 * 自定义 FastJson 的配置:
 * 大整数转换为字符串，因为 JS 中 long 会溢出
 */
public class FastJsonConfig extends com.alibaba.fastjson.support.config.FastJsonConfig {
    public FastJsonConfig(){
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);

        this.setSerializeConfig(serializeConfig);
    }
}
