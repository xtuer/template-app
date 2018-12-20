#-------------------------------------------
# 表名：school
# 作者：黄彪
# 日期：2018-03-24
# 版本：1.0
# 描述：保存学校
#------------------------------------------
DROP TABLE IF EXISTS school;

CREATE TABLE school (
    id bigint(20) unsigned NOT NULL         COMMENT '学校的 ID',
    admin_id bigint(20) unsigned            COMMENT '管理员帐号的 ID',
    name     varchar(128) NOT NULL          COMMENT '学校的名字',
    abbreviation_name varchar(256)          COMMENT '学校的简称',
    host           varchar(128)             COMMENT '学校的域名',
    port           int DEFAULT 80           COMMENT '端口',
    title          varchar(1024)            COMMENT '网站标题',
    logo_url       varchar(256)             COMMENT '网站 Logo 的 URL',
    image_url      varchar(256)             COMMENT '网站主题图的 URL',
    icp_license    varchar(1024)            COMMENT '网站备案',
    links          text                     COMMENT '快速链接，使用数组的 JSON 字符串',
    founding_year  int                      COMMENT '建校时间',
    contact_person varchar(128)             COMMENT '联系人',
    contact_phone  varchar(128)             COMMENT '联系电话',
    contact_email  varchar(128)             COMMENT '联系邮件',
    is_enabled     tinyint DEFAULT 1        COMMENT '是否可用',
    is_message_enabled tinyint DEFAULT 0    COMMENT '是否开启短信登录',
    is_library_enabled tinyint DEFAULT 0    COMMENT '是否启用数字图书馆',
    message_template   text                 COMMENT '短信模板',
    education_types    text                 COMMENT '教育类型，多个类型间使用逗号分割',
    teacher_count int DEFAULT 0             COMMENT '老师的数量',
    student_count int DEFAULT 0             COMMENT '学生的数量',
    created_time datetime DEFAULT NULL      COMMENT '创建时间',
    updated_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)                        COMMENT '学校的 ID 作为主键',
    UNIQUE KEY host_unique (host)           COMMENT '学校的域名唯一'
) ENGINE=InnoDB;
