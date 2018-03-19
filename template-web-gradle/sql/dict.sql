#-------------------------------------------
# 表名：dict
# 作者：二狗
# 日期：2018-03-07
# 版本：1.0
# 描述：保存字典数据
#------------------------------------------
CREATE TABLE dict (
    id    bigint(20) unsigned NOT NULL COMMENT '字典的 ID',
    code  varchar(128) NOT NULL        COMMENT '字典的编码',
    value varchar(256) NOT NULL        COMMENT '字典的值',
    type  varchar(128) NOT NULL        COMMENT '字典的类型',
    description text                   COMMENT '字典的描述',
    PRIMARY KEY (id),
    UNIQUE KEY dict_identifier (code, type) COMMENT 'code + type 唯一标记一个字典数据',
    KEY idx_type (type) COMMENT '类型建立索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
