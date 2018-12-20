package edu.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;

import java.math.BigInteger;

public class FastJsonConfigExt extends FastJsonConfig {
    public FastJsonConfigExt(){
        super();
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        serializeConfig.put(Double.class, ToStringSerializer.instance);
        serializeConfig.put(Double.TYPE, ToStringSerializer.instance);
        this.setSerializeConfig(serializeConfig);
    }
}
