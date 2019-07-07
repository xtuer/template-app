package edu.bean;

import org.apache.commons.text.StringSubstitutor;

import java.util.Collections;

/**
 * Redis 使用的缓存的 key
 */
public class RedisKey {
    // 缓存时间
    public static long ONE_MINUTE_IN_SECONDS = 60;      // 1 分钟
    public static long ONE_HOUR_IN_SECONDS   = 3600;    // 1 小时的秒数
    public static long ONE_DAY_IN_SECONDS    = 86400;   // 1 天的秒数
    public static long ONE_MONTH_IN_SECONDS  = 2592000; // 1 个月的秒数
    public static long HALF_HOUR_IN_SECONDS  = 1800;    // 半小时的秒数
    public static long HALF_DAY_IN_SECONDS   = 43200;   // 半天的秒数

    private static String USER = "user:${id}";  // 用户
    private static String ORG  = "org:${host}"; // 机构 (使用 host)
    private static String CLAZZ_STUDENTS = "clazz:${clazzId}:students"; // 班级学生 ID 列表缓存
    private static String CONVERT_FILE_PROGRESS = "convert_progress:";  // 转换文件进度
    private static String CONVERT_FILE_ERROR    = "convert_error:";     // 转换文件错误

    /**
     * 机构的 key，如 org:edu.com
     *
     * @param host 机构的域名
     * @return 返回 key
     */
    public static String orgKey(String host) {
        return StringSubstitutor.replace(ORG, Collections.singletonMap("org", host));
    }

    /**
     * 用户的 key，如 user:332516032017072128
     *
     * @param userId 用户 ID
     * @return 返回 key
     */
    public static String userKey(long userId) {
        return StringSubstitutor.replace(USER, Collections.singletonMap("id", userId));
    }
}
