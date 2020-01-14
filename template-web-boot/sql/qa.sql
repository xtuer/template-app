#-------------------------------------------
# 表名：qa_question
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：答疑的问题，分为 2 类: 问答和投票
#------------------------------------------
DROP TABLE IF EXISTS qa_question;

CREATE TABLE qa_question (
    id        bigint(20)   NOT NULL  COMMENT '问题 ID',
    content   text                   COMMENT '内容',
    clazz_id  bigint(20)   NOT NULL  COMMENT '班级 ID',
    type      tinyint      DEFAULT 0 COMMENT '类型: 0 (问答)、1 (投票)',
    user_id   bigint(20)   NOT NULL  COMMENT '创建人 ID',
    username  varchar(256) NOT NULL  COMMENT '创建人名字，方便显示',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id) COMMENT '问题 ID 作为主键',
    KEY index_clazz_id (clazz_id) COMMENT '加速查找问题的回复'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：qa_reply
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：问题的回复
#------------------------------------------
DROP TABLE IF EXISTS qa_reply;

CREATE TABLE qa_reply (
    id           bigint(20)   NOT NULL  COMMENT '回复 ID',
    content      text                   COMMENT '内容',
    question_id  bigint(20)   NOT NULL  COMMENT '问题 ID',
    parent_id    bigint(20)   DEFAULT 0 COMMENT '被回复 ID: 可对回复进行回复，第一级回复的 parent_id 为 0',
    top_reply_id bigint(20)   NOT NULL  COMMENT '第一级回复 ID',
    user_id      bigint(20)   NOT NULL  COMMENT '回复人 ID',
    username     varchar(256) NOT NULL  COMMENT '回复人名字，方便显示',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id) COMMENT '回复 ID 作为主键',
    KEY index_question_id (question_id) COMMENT '加速查找问题的回复'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：qa_vote_item
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：投票项
#      问题类型为 1 时为投票，每个投票有 n 个项
#------------------------------------------
DROP TABLE IF EXISTS qa_vote_item;

CREATE TABLE qa_vote_item (
    id          bigint(20) NOT NULL  COMMENT '投票项 ID',
    content     text                 COMMENT '内容',
    question_id bigint(20) NOT NULL  COMMENT '问题 ID',
    position    int        DEFAULT 0 COMMENT '位置',
    vote_count  int        DEFAULT 0 COMMENT '投票数',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id) COMMENT '回复 ID 作为主键'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：qa_vote_record
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：投票记录
#      某人对某个问题的某项进行投票的记录，同一个用户对一个选项只能投票一次
#------------------------------------------
DROP TABLE IF EXISTS qa_vote_record;

CREATE TABLE qa_vote_record (
    id           bigint(20)   NOT NULL COMMENT '投票记录 ID',
    question_id  bigint(20)   NOT NULL COMMENT '问题 ID',
    vote_item_id bigint(20)   NOT NULL COMMENT '投票项 ID',
    user_id      bigint(20)   NOT NULL COMMENT '投票人 ID',
    username     varchar(256) NOT NULL COMMENT '投票人名字，方便显示',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id) COMMENT '投票记录 ID 作为主键',
    UNIQUE  KEY index_user_vote (user_id, vote_item_id) COMMENT '同一个用户对一个选项只能投票一次'
) ENGINE=InnoDB;

#-------------------------------------------
# 表名：qa_action_record
# 作者：黄彪
# 日期：2019-07-05
# 版本：1.0
# 描述：答疑的操作记录
#      例如点赞、喜欢、浏览问题记录，甚至投票记录也可存放到这里而不需要单独的表
#------------------------------------------
DROP TABLE IF EXISTS qa_action_record;

CREATE TABLE qa_action_record (
    target_id   bigint(20)   NOT NULL  COMMENT '目标 ID',
    ref_id      bigint(20)   DEFAULT 0 COMMENT '相关 ID，例如目标为选项，ref_id 可为它的问题 ID',
    type        tinyint      DEFAULT 0 COMMENT '类型: 0 (浏览-问题)、1 (点赞-问题)、2 (点赞-回复)、3 (投票)',
    is_canceled tinyint      DEFAULT 0 COMMENT '取消: 0 (取消)、1 (未取消)，例如先点击喜欢设置为 0，再次点击喜欢设置为 1',
    user_id     bigint(20)   NOT NULL  COMMENT '用户 ID',
    username    varchar(256) NOT NULL  COMMENT '用户名字，方便显示',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    id int(11) PRIMARY KEY AUTO_INCREMENT COMMENT '无意义的主键 ID',
    UNIQUE KEY index_user_target (user_id, target_id) COMMENT '同一个用户对一个目标只能有一个操作记录'
) ENGINE=InnoDB;
