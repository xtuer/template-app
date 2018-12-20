#-------------------------------------------
# 表名：user
# 作者：黄彪
# 日期：2018-03-07
# 版本：1.0
# 描述：用户表
#      除了系统管理员，其他用户都和学校相关
#      用户名可以重复，但是同一个学校用户名不能重复，对 school_id + username 建立了唯一索引
#------------------------------------------
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id         bigint(20) unsigned NOT NULL COMMENT '用户的 ID',
    username   varchar(128)        NOT NULL COMMENT '名字',
    password   varchar(128)        NOT NULL COMMENT '密码',
    nickname   varchar(256)                 COMMENT '昵称',
    email      varchar(256)                 COMMENT '邮件地址',
    mobile     varchar(64)                  COMMENT '手机号码',
    phone      varchar(64)                  COMMENT '固定电话',
    role       varchar(128)                 COMMENT '角色',
    avatar     varchar(512)                 COMMENT '头像的 URL',
    gender     int DEFAULT 0                COMMENT '性别: 0(未设置), 1(男), 2(女)',
    school_id  bigint(20) DEFAULT 0         COMMENT '所属学校的 ID: 默认为 0',
    is_enabled tinyint DEFAULT 1            COMMENT '1 为启用，0 为禁用',

    login_count int default 0               COMMENT '登录次数',
    login_time  datetime DEFAULT NULL       COMMENT '最后登录时间',

    created_time datetime DEFAULT NULL      COMMENT '创建时间',
    updated_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id)                        COMMENT '用户的 ID 作为主键',
    UNIQUE  KEY idx_user_identifier (school_id, username) COMMENT '同一个学校用户名不能重复'
) ENGINE=InnoDB;

# 初始化时创建系统管理员: admin/admin
INSERT INTO user (id, username, nickname, password, role, school_id, created_time) VALUES (1, 'admin', '系统管理员', '{bcrypt}$2a$10$KYIBStaQwdYEetYcKlb/Uu0vENXOTxdvaAfnOrZlvsDoVUfmuXIHi', 'ROLE_ADMIN_SYSTEM', 0, now());
