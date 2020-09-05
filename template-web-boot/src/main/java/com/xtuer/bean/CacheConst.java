package com.xtuer.bean;

/**
 * 缓存使用的键和缓存的名字
 */
public interface CacheConst {
    // 默认的缓存对象，针对每一个缓存进行统计: CacheManager -> Cache -> CacheElement
    // 如果要做的更细致，不同的业务使用不同的 Cache 对象
    String CACHE = "xtuer:";

    // 用户机构
    String KEY_USER_ID  = "'user.' + #userId";
    String KEY_ORG_HOST = "'org.' + #host";

    // 考试
    String KEY_EXAM     = "'exam.' + #exam.examId";
    String KEY_EXAM_ID  = "'exam.' + #examId";
    String KEY_PAPER    = "'paper.' + #paper.paperId";
    String KEY_PAPER_ID = "'paper.' + #paperId";
}
