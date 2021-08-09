#-------------------------------------------
# 表名：WORKER_NODE
# 作者：黄彪
# 日期：2020-05-04
# 版本：1.0
# 描述：百度基于 Snowflake 的 Uid Generator 的表
#------------------------------------------
DROP TABLE IF EXISTS WORKER_NODE;

CREATE TABLE WORKER_NODE (
    ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
    HOST_NAME VARCHAR(64) NOT NULL COMMENT 'host name',
    PORT VARCHAR(64) NOT NULL COMMENT 'port',
    TYPE INT NOT NULL COMMENT 'node type: ACTUAL or CONTAINER',
    LAUNCH_DATE DATE NOT NULL COMMENT 'launch date',
    MODIFIED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'modified time',
    CREATED TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
    PRIMARY KEY (ID)
) COMMENT = 'DB WorkerID Assigner for UID Generator', ENGINE = INNODB;
