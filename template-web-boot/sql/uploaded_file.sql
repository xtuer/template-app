#-------------------------------------------
# 表名：uploaded_file
# 作者：黄彪
# 日期：2019-07-07
# 版本：1.0
# 描述：上传的文件信息表
#------------------------------------------
DROP TABLE IF EXISTS uploaded_file;

CREATE TABLE uploaded_file (
    id       bigint(20)   NOT NULL  COMMENT '每个上传的文件都有一个唯一的 ID',
    filename varchar(256) NOT NULL  COMMENT '文件的原始名字',
    url      varchar(256) NOT NULL  COMMENT '访问文件的 URL',
    type     int(11)      DEFAULT 0 COMMENT '文件的类型: 0 (临时文件), 1 (系统管理员上传的文件), 2 (老师上传的文件), 3 (学生上传的文件)',
    user_id  bigint(20)   DEFAULT 0 COMMENT '上传文件的用户 ID',

    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id) COMMENT '文件的 ID 作为主键'
) ENGINE=InnoDB;
