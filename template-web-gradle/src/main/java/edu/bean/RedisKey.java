package edu.bean;

/**
 * Redis 使用的缓存的 key
 */
public interface RedisKey {
    // 缓存时间
    long ONE_MINUTE_IN_SECONDS = 60;      // 1 分钟
    long ONE_HOUR_IN_SECONDS   = 3600;    // 1 小时的秒数
    long ONE_DAY_IN_SECONDS    = 86400;   // 1 天的秒数
    long ONE_MONTH_IN_SECONDS  = 2592000; // 1 个月的秒数
    long HALF_HOUR_IN_SECONDS  = 1800;    // 半小时的秒数
    long HALF_DAY_IN_SECONDS   = 43200;   // 半天的秒数

    String SCHOOL_HOST        = "school:";               // 学校缓存 (使用 host)
    String CLAZZ_STUDENTS     = "clazz:%s:students";     // 班级学生 ID 列表缓存
    String COURSEWARE_SUBJECT = "courseware:%s:subject"; // 课件学科缓存
    String UPLOADED_FILE      = "uploaded_file:";        // 上传的文件信息缓存
    String CONVERT_FILE_PROGRESS = "convert_progress:";  // 转换文件进度
    String CONVERT_FILE_ERROR    = "convert_error:";     // 转换文件错误
}
