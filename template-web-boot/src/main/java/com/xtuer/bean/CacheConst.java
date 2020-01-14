package com.xtuer.bean;

/**
 * 缓存使用的键和缓存的名字
 */
public interface CacheConst {
    String CACHE = "training:"; // 默认的缓存对象

    String KEY_USER_ID  = "'user.' + #userId";
    String KEY_ORG_HOST = "'org.' + #host";
}
