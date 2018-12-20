#-------------------------------------------
# 表名：uploaded_file
# 作者：黄彪
# 日期：2018-08-25
# 版本：1.0
# 描述：保存上传文件的信息
#------------------------------------------
DROP TABLE IF EXISTS uploaded_file;

CREATE TABLE uploaded_file (
    id bigint(20) unsigned    NOT NULL COMMENT '每个上传的文件都有一个唯一的 ID',
    filename     varchar(256) NOT NULL COMMENT '文件的原始名字',
    url          varchar(256) NOT NULL COMMENT '访问文件的 URL',
    type         int DEFAULT 0         COMMENT '文件的类型: 0 为临时文件，1 为平台的正式文件，2 为老师上传的正式文件',
    user_id      bigint(20) unsigned   COMMENT '上传文件的用户的 ID',
    created_time datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id)                   COMMENT '文件的 ID 作为主键'
) ENGINE=InnoDB;
