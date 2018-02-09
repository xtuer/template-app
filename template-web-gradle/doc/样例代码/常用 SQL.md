[TOC]


## 分页查询

MySQL 中使用 LIMIT 进行分页，第一个参数是起始位置 offset，第二个参数是要取多少条记录

```sql
SELECT * FROM question WHERE subject_code='XXX' LIMIT 0, 30
```

## 保存更新

查看 UNIQUE 索引或 PRIMARY KEY 对应的行是否存在，存在则更新(执行 ON DUPLICATE KEY UPDATE 后面的语句)，不存在则插入新行

```sql
# id 是唯一主键
INSERT INTO question (id, type, content) VALUES (#{id}, #{type}, #{content})
ON DUPLICATE KEY UPDATE content=#{content}
```

插入时先使用条件查询，满足条件时才插入，不满足条件就不进行插入

```sql
# 根据条件查询，满足条件时才插入
INSERT INTO paper_knowledge_point_relation(paper_id, knowledge_point_id, tenant_code)
SELECT #{paperId}, #{knowledgePointId}, #{tenantCode}
FROM   dual
WHERE NOT EXISTS(
    SELECT 1 FROM paper_knowledge_point_relation
    WHERE paper_id=#{paperId} AND knowledge_point_id=#{knowledgePointId} AND tenant_code=#{tenantCode}
)
```

## 联合更新

查询的结果作为临时表，更新知识点下的题目数量

```sql
UPDATE question_knowledge_point qkp
JOIN   (SELECT knowledge_point_id AS id, COUNT(id) AS count FROM question GROUP BY knowledge_point_id) AS t
ON     qkp.id=t.id
SET    qkp.count=t.count
```

> 使用了子查询

## 左连接

查询所有题目及它的选项

```sql
SELECT q.id, q.content, qo.id, qo.content
FROM   question q
LEFT JOIN question_option qo ON q.id=qo.question_id
```

## 内连接

内连接和 where 等价，查询所有有选项的题目

```sql
SELECT q.id, q.content, qo.id, qo.content
FROM   question q
JOIN   question_option qo ON q.id=qo.question_id

SELECT q.id, q.content, qo.id, qo.content
FROM   question q, question_option qo
WHERE  q.id=qo.question_id
```

> 可参考 <https://www.cnblogs.com/eflylab/archive/2007/06/25/794278.html>

## 分组

统计有选项的题目的选项个数

```sql
SELECT q.id, count(1), qo.id, qo.content
FROM   question q
JOIN   question_option qo ON q.id=qo.question_id
GROUP BY q.id
```

> 数据量大时 JOIN 比 LEFT JOIN 快很多

## 类型转换

使用 `CAST` 转换类型

```sql
SELECT CAST(id AS CHAR) AS id FROM question
```

## 字符串连接

使用 `CONCAT(p1, p2, p3)` 连接字符串

```sql
SELECT CONCAT(subject_code, '-', original_id) FROM question WHERE is_marked=1
```

## 返回布尔值

JDBC 标准中，`0 表示 false，1 表示 true`，大于 1 和小于 0 的数没有定义，MySQL 的 JDBC Driver 中 <=0 表示 false，>=1 表示 true，为了保险起见，使用 EXISTS 来查询返回布尔值

```sql
SELECT EXISTS (
    SELECT 1 FROM paper WHERE paper_id=#{paperId}
)
```

## 建表语句

建表语句中需要有足够的注释描述每一列的作用，便于维护

```sql
#-------------------------------------------
# 表名：question
# 作者：公孙二狗
# 日期：2018-04-01
# 版本：1.0
# 描述：保存题目
#------------------------------------------
DROP TABLE IF EXISTS question;

CREATE TABLE question (
    id bigint(20) unsigned NOT NULL COMMENT '题目 ID',
    type varchar(8) DEFAULT ''      COMMENT '题目类型',
    content mediumtext              COMMENT '题目内容：题干+选项',
    analysis mediumtext             COMMENT '题目解析',
    answer text                     COMMENT '题目答案',
    demand varchar(32) DEFAULT ''   COMMENT '教学要求',
    score int(11) DEFAULT 0         COMMENT '题目分值',
    difficulty int(11) DEFAULT 0    COMMENT '题目难度',
    original_id varchar(64) DEFAULT  ''        COMMENT '题目在乐教乐学数据库中的 ID',
    subject_code varchar(64) DEFAULT ''        COMMENT '题目的科目编码',
    knowledge_point_code varchar(8) DEFAULT '' COMMENT '题目的知识点编码',
    knowledge_point_id bigint(20) DEFAULT 0    COMMENT '题目的知识点 ID',
    is_marked tinyint(4) DEFAULT 0             COMMENT '是否被标记过，0 为未标记，1 为已标记',
    created_time datetime DEFAULT NULL         COMMENT '创建时间',
    updated_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT '存储题目的表';
```

## 添加索引

* 唯一索引

  ```sql
  ALTER TABLE `table_name` ADD UNIQUE (`column`)
  ```

* 普通索引

  ```sql
  ALTER TABLE `table_name` ADD INDEX index_name (`column`)
  ```

* 多列索引

  ```sql
  ALTER TABLE `table_name` ADD INDEX index_name (`column1`, `column2`, `column3`)
  ```

* 建表时用 `KEY` 创建

  ```sql
  CREATE TABLE `demo` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `info` text COLLATE utf8_unicode_ci NOT NULL,
      `is_marked` tinyint(11) DEFAULT '0',
      `count` int(11) DEFAULT NULL,
      `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`),
      KEY `idx_modified_at` (`modified_at`),
      KEY `idx_count` (`count`)
  ) ENGINE=InnoDB
  ```

## 删除索引

```sql
ALTER TABLE table_name DROP INDEX index_name
```

## 创建视图

```sql
DROP VIEW IF EXISTS view_paper_knowledge_point;

CREATE VIEW view_paper_knowledge_point
AS SELECT
    pkpr.paper_id AS paper_id,
    kp.knowledge_point_id AS knowledge_point_id,
    kp.name AS name,
    kp.tenant_code AS tenant_code
FROM paper_knowledge_point_relation pkpr
LEFT JOIN knowledge_point kp ON pkpr.knowledge_point_id = kp.knowledge_point_id
WHERE kp.is_deleted=0;
```



