# 考试相关总共有下面 8 个表
# exam_question
# exam_question_item
# exam_paper_question
# exam_paper
# exam
# exam_record
# exam_question_answer
# exam_question_result

#-------------------------------------------
# 表名：exam_question
# 作者：黄彪
# 日期：2019-06-21
# 版本：1.0
# 描述：题目
#      客观题 (单选题、多选题、判断题): 有多个选项
#      主观题 (填空题、问答题):
#             填空题: 每个空对应一个选项
#             问答题: 只有一个选项，针对这个选项进行回答
#      标题: 没有选项，只是用来组织试卷用的，不可回答
#------------------------------------------
DROP TABLE IF EXISTS exam_question;

CREATE TABLE exam_question (
    id         bigint(20) NOT NULL   COMMENT '题目 ID',
    stem       text                  COMMENT '题干',
    `key`      text                  COMMENT '参考答案',
    analysis   text                  COMMENT '题目解析',
    type       tinyint(11) DEFAULT 0 COMMENT '题目类型: 0 (未知)、1 (单选题)、2 (多选题)、3 (判断题)、4 (填空题)、5 (问答题)、6 (复合题)、7 (题型题)',
    difficulty tinyint(11) DEFAULT 0 COMMENT '题目难度: 0 (未知)、1 (容易)、2 (较易)、3 (一般)、4 (较难)、5 (困难)',
    position   int(11)     DEFAULT 0 COMMENT '复合题的小题在题目中的位置',
    purpose    int(11)     DEFAULT 0 COMMENT '题目用途: 0 (考试题目)、1 (问卷题目)',
    parent_id  bigint(20)  DEFAULT 0 COMMENT '复合题的小题所属大题 ID',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY index_parent_id (parent_id) COMMENT '加速查找复合题的小题'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：exam_question_option
# 作者：黄彪
# 日期：2019-06-21
# 版本：1.0
# 描述：题目的选项，每个题目默认都有个位置属性，非打乱选项顺序时用
#------------------------------------------
DROP TABLE IF EXISTS exam_question_option;

CREATE TABLE exam_question_option (
    id          bigint(20)  NOT NULL  COMMENT '选项 ID',
    `desc`      text                  COMMENT '选项描述',
    is_correct  tinyint(11) DEFAULT 0 COMMENT '是否正确选项',
    question_id bigint(20)  NOT NULL  COMMENT '所属题目 ID',
    position    int(11)     DEFAULT 0 COMMENT '选项在题目中的位置',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY index_question_id (question_id) COMMENT '加速查找题目的选项'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：exam_paper_question
# 作者：黄彪
# 日期：2018-06-21
# 版本：1.0
# 描述：试卷的题目
#      包含的信息有如题目在某个试卷里的位置、分值、分组 (例如属于单选题) 等
#------------------------------------------
DROP TABLE IF EXISTS exam_paper_question;

CREATE TABLE exam_paper_question (
    paper_id           bigint(20)   DEFAULT 0  COMMENT '试卷 ID',
    question_id        bigint(20)   DEFAULT 0  COMMENT '题目 ID',
    group_sn           int(11)      DEFAULT 0  COMMENT '大题 (题型) 分组序号，例如属于第一大题单选题组，顺序表示在试卷中的位置',
    position           int(11)      DEFAULT 0  COMMENT '题目在试卷里的位置',
    score              double       DEFAULT 0  COMMENT '每题得分 (创建试卷时题型题下的题目每题得分，方便构造题干): 单选题，每题 5 分，共 30 分',
    total_score        double       DEFAULT 0  COMMENT '题目满分 (题目、题型题、复合题的满分)',
    sn_label           varchar(128) DEFAULT '' COMMENT '试卷中题目的可读序号，例如 一、二、1、2、❶、❷ 等',
    parent_question_id bigint(20)   DEFAULT 0  COMMENT '小题所属复合题的 ID',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    id int(11) PRIMARY KEY AUTO_INCREMENT COMMENT '无意义的主键 ID',
    KEY index_paper_id (paper_id),
    UNIQUE KEY index_paper_question (paper_id, question_id) COMMENT '同一个题在同一份试卷中只能出现一次'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：exam_paper
# 作者：黄彪
# 日期：2019-06-21
# 版本：1.0
# 描述：试卷
#      只有试卷 ID 是关键，在 exam_paper_question 中使用，表示题目属于某个试卷
#------------------------------------------
DROP TABLE IF EXISTS exam_paper;

CREATE TABLE exam_paper (
    id            bigint(20)    NOT NULL   COMMENT '试卷 ID',
    title         varchar(2048) DEFAULT '' COMMENT '试卷标题',
    type          int(11)       DEFAULT 0  COMMENT '试卷类型: 0 (普通试卷)、1 (调查问卷)',
    total_score   int(11)       DEFAULT 0  COMMENT '试卷总分',
    is_subjective tinyint(4)    DEFAULT 0  COMMENT '0 (全是客观题)、1 (包含主观题)',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id)
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：exam
# 作者：黄彪
# 日期：2019-06-21
# 版本：1.0
# 描述：考试
#      包含试卷、班级、以及考试相关的信息组成，例如考试时间、是否允许查看答案、是否打乱题目顺序、是否打乱选项顺序，
#      考试时间为在 start_time 和 end_time 之间，最多考 duration 分钟，允许考几次等
#------------------------------------------
DROP TABLE IF EXISTS exam;

CREATE TABLE exam (
    id                          bigint(20)                  COMMENT '考试 ID',
    paper_id                    bigint(20)    DEFAULT 0     COMMENT '试卷 ID',
    clazz_id                    bigint(20)    DEFAULT 0     COMMENT '班级 ID',
    title                       varchar(2048) DEFAULT ''    COMMENT '考试标题',
    start_time                  datetime      DEFAULT NULL  COMMENT '考试开始时间',
    end_time                    datetime      DEFAULT NULL  COMMENT '考试结束时间',
    duration                    int(11)       DEFAULT 0     COMMENT '考试持续时间, 单位为秒',
    allowed_times               int(11)       DEFAULT 1     COMMENT '允许考试次数',

    is_question_shuffled        tinyint(4)    DEFAULT 0     COMMENT '是否打乱题目',
    is_question_option_shuffled tinyint(4)    DEFAULT 0     COMMENT '是否打乱题目的选项',
    is_correction_finished      tinyint(4)    DEFAULT 0     COMMENT '是否已批改完试卷: 0 (未完成)、1 (完成)',
    highest_score               double        DEFAULT 0     COMMENT '最高分',
    lowest_score                double        DEFAULT 0     COMMENT '最低分',
    avg_score                   double        DEFAULT 0     COMMENT '平均分',
    pass_rate                   double        DEFAULT 0     COMMENT '及格率',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY index_clazz_id (clazz_id) COMMENT '加速查找班级的试卷'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：exam_record
# 作者：黄彪
# 日期：2019-06-21
# 版本：1.0
# 描述：考试记录
#      保存学生考试的记录，同一个试卷可以考多次，考试记录用来确定某一次考试
#------------------------------------------
DROP TABLE IF EXISTS exam_record;

CREATE TABLE exam_record (
    id             bigint(20) NOT NULL     COMMENT '试卷记录 ID',
    user_id        bigint(20) DEFAULT 0    COMMENT '考试用户 ID',
    exam_id        bigint(20) DEFAULT 0    COMMENT '考试 ID',
    clazz_id       bigint(20) DEFAULT 0    COMMENT '班级 ID',
    paper_id       bigint(20) DEFAULT 0    COMMENT '试卷 ID',
    score          double     DEFAULT 0    COMMENT '考试得分',
    status         int        DEFAULT 0    COMMENT '状态: 0 (已创建)、1 (已提交)、2 (已批改) [点击考试的时候才创建考试记录]',
    used_time      int(11)    DEFAULT 0    COMMENT '已使用时间，单位为秒',
    clazz_rank     int(11)    DEFAULT 0    COMMENT '班级排名',
    submitted_time datetime   DEFAULT NULL COMMENT '提交试卷时间',
    nickname       varchar(256) DEFAULT '' COMMENT '用户昵称',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY index_user_exam_record (user_id, exam_id) COMMENT '加速查找用户的某次考试的所有考试记录'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：exam_question_answer
# 作者：黄彪
# 日期：2019-06-21
# 版本：1.0
# 描述：题目作答表
#      记录用户什么时候、回答什么试卷 (作业) 的题目，不管是主观题还是客观题，回答的选项保存到作答表中，主观题的选项回答的内容保存到 content 里
#      保存投票和调查问卷等时，不需要创建考试记录，只需要自定义一个 record_id 即可，这个 record_id 可以保存在其他地方
#
#      客观题 (单选题、多选题、判断题): 保存选择的选项
#      主观题 (填空题、问答题):
#             填空题: 每个空对应一个选项
#             问答题: 只有一个选项，针对这个选项进行回答
#      保存回答时，先删除对应题目的所有回答，然后再把新的所有回答保存一次
#------------------------------------------
DROP TABLE IF EXISTS exam_question_answer;

CREATE TABLE exam_question_answer (
    user_id            bigint(20) DEFAULT 0 COMMENT '考试用户 ID',
    record_id          bigint(20) DEFAULT 0 COMMENT '考试记录 ID',
    paper_id           bigint(20) DEFAULT 0 COMMENT '试卷 ID',
    question_id        bigint(20) DEFAULT 0 COMMENT '题目 ID',
    question_option_id bigint(20) DEFAULT 0 COMMENT '选项 ID',
    content            text                 COMMENT '主观题的回答内容，客观题时为空',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    id int(11) PRIMARY KEY AUTO_INCREMENT COMMENT '无意义的主键 ID',
    UNIQUE KEY index_record_option_unique (record_id, question_option_id) COMMENT '记录作答问题选项唯一',
    KEY index_question_answer (user_id, record_id, paper_id)
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：exam_question_result
# 作者：黄彪
# 日期：2019-06-21
# 版本：1.0
# 描述：题目作答分数表，记录用户什么时候、回答什么试卷 (作业) 的题目所获得的分数
#------------------------------------------
DROP TABLE IF EXISTS exam_question_result;

CREATE TABLE exam_question_result (
    user_id     bigint(20) DEFAULT 0 COMMENT '考试用户 ID',
    record_id   bigint(20) DEFAULT 0 COMMENT '考试记录 ID',
    paper_id    bigint(20) DEFAULT 0 COMMENT '试卷 ID',
    question_id bigint(20) DEFAULT 0 COMMENT '题目 ID',
    score       double     DEFAULT 0 COMMENT '分数',
    status      tinyint(4) DEFAULT 0 COMMENT '题目的作答状态: 0 (错)、1 (半对)、2 (对)',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    id int(11) PRIMARY KEY AUTO_INCREMENT COMMENT '无意义的主键 ID',
    KEY index_question_result (user_id, record_id, paper_id)
) ENGINE=InnoDB;
