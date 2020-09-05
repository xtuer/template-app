#--------------------------------------------------------------------------------------
# 表名：organization
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：机构
#      organization 缩写 org
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS organization;

CREATE TABLE organization (
    org_id    bigint(20)   NOT NULL COMMENT '机构 ID',
    name      varchar(128) NOT NULL COMMENT '机构名字',
    host      varchar(128)          COMMENT '机构域名',
    port      int DEFAULT 80        COMMENT '网站端口',
    admin_id  bigint(20) DEFAULT 0  COMMENT '管理员 ID',
    parent_id bigint(20) DEFAULT 0  COMMENT '上级机构 ID',

    contact_person varchar(128) NOT NULL COMMENT '单位对接人名字',
    contact_mobile varchar(32)  NOT NULL COMMENT '单位对接人电话',
    portal_name    varchar(128) NOT NULL COMMENT '门户平台名称',
    logo           varchar(256)          COMMENT 'Logo',
    enabled        tinyint DEFAULT 1     COMMENT '状态: 0 (禁用), 1 (启用)',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (org_id) COMMENT '机构 ID 作为主键',
    UNIQUE  KEY index_host_unique (host) COMMENT '机构域名唯一'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名：user
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：用户
#      每个用户只属于一个机构
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    user_id  bigint(20)   NOT NULL COMMENT '用户 ID',
    username varchar(128) NOT NULL COMMENT '账号',
    nickname varchar(256) NOT NULL COMMENT '昵称',
    password varchar(128) NOT NULL COMMENT '密码',
    email    varchar(256)          COMMENT '邮件地址',
    mobile   varchar(64)           COMMENT '手机号码',
    phone    varchar(64)           COMMENT '固定电话',
    avatar   varchar(512)          COMMENT '头像 URL',
    gender   tinyint DEFAULT 0     COMMENT '性别: 0 (未设置)、1 (男)、2 (女)',
    org_id   bigint(20)  NOT NULL  COMMENT '机构 ID',
    enabled  tinyint DEFAULT 1     COMMENT '状态: 0 (禁用), 1 (启用)',

    created_at datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (user_id) COMMENT '用户的 ID 作为主键',
    UNIQUE  KEY index_org_user (org_id, username) COMMENT '同一个机构用户名不能重复'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名：role
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：角色表
# 注意：角色的字符串需要前缀 ROLE_，例如 ROLE_ADMIN_SYSTEM
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS role;

CREATE TABLE role (
    name  varchar(128) COMMENT '角色名字',
    value varchar(128) COMMENT '角色的值',

    PRIMARY KEY (name) COMMENT '角色不能重复'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名：user_role
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：用户角色表
#      一个用户可以有多个角色
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS user_role;

CREATE TABLE user_role (
    user_id bigint(20)   NOT NULL COMMENT '用户 ID',
    role    varchar(128) NOT NULL COMMENT '角色',

    id int(11) PRIMARY KEY AUTO_INCREMENT COMMENT '无意义的主键 ID',
    UNIQUE KEY index_user_role (user_id, role) COMMENT '用户的角色不能重复'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 表名：user_login
# 作者：黄彪
# 日期：2019-06-20
# 版本：1.0
# 描述：用户登录记录
#--------------------------------------------------------------------------------------
DROP TABLE IF EXISTS user_login;

CREATE TABLE user_login (
    user_id bigint(20)    NOT NULL COMMENT '用户 ID',
    username varchar(128) NOT NULL COMMENT '账号',
    created_at datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    id int(11) PRIMARY KEY AUTO_INCREMENT COMMENT '无意义的主键 ID'
) ENGINE=InnoDB;

#--------------------------------------------------------------------------------------
# 初始化
#--------------------------------------------------------------------------------------
# 1. 创建系统管理员: admin/admin, org_id 为 1 表示系统管理平台
INSERT INTO user (user_id, username, nickname, password, org_id)
VALUES (1, 'admin', '系统管理员', '{bcrypt}$2a$10$KYIBStaQwdYEetYcKlb/Uu0vENXOTxdvaAfnOrZlvsDoVUfmuXIHi', 1);

# 2. admin 的角色为系统管理员
INSERT INTO user_role (user_id, role)
VALUES (1, 'ROLE_ADMIN_SYSTEM');

# 3. 创建角色
INSERT INTO role (name, value)
VALUES ('ROLE_ADMIN_SYSTEM', '系统管理员'),
       ('ROLE_ADMIN_ORG',    '机构管理员'),
       ('ROLE_USER',         '普通用户'),
       ('ROLE_STUDENT',      '学生'),
       ('ROLE_TEACHER',      '老师');

#--------------------------------------------------------------------------------------
# SQL 示例
#--------------------------------------------------------------------------------------
# 获取用户和他的角色
# SELECT * FROM user LEFT JOIN user_role ON user.user_id = user_role.user_id
