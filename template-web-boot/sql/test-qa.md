# 答疑测试

## 问题和回复

```
1. 特别定制「乌龙茶」,有人跟我一样只喜欢有味道的，或者冰的饮品嘛？
    1. 东方树叶，每天基本都会喝 1，2 瓶
    2. 抹茶粉确实很流行，但是和我提的浓缩粉其实是两种产品
        3. 炭焙的一般口味略重
        4. 好处就是完全不伤胃
2. 找一个靠谱的设计师长期合作
    5. 明天早上起来加你
        6. 怎么联系
    7. 本人做一些兼职
```

## 清空数据库

```sql
TRUNCATE TABLE qa_question;
TRUNCATE TABLE qa_reply;
```

## 问题一

```sql
INSERT INTO qa_question(id, clazz_id, user_id, user_name, content) VALUES (1, 1, 30, 'Bob', '特别定制「乌龙茶」,有人跟我一样只喜欢有味道的，或者冰的饮品嘛？');

INSERT INTO qa_reply(id, question_id, parent_id, top_reply_id, user_id, user_name, content) VALUES (1, 1, 0, 1, 31, 'Tom', '东方树叶，每天基本都会喝 1，2 瓶');
INSERT INTO qa_reply(id, question_id, parent_id, top_reply_id, user_id, user_name, content) VALUES (2, 1, 0, 2, 31, 'Tom', '抹茶粉确实很流行，但是和我提的浓缩粉其实是两种产品');
INSERT INTO qa_reply(id, question_id, parent_id, top_reply_id, user_id, user_name, content) VALUES (3, 1, 2, 2, 31, 'Tom', '炭焙的一般口味略重');
INSERT INTO qa_reply(id, question_id, parent_id, top_reply_id, user_id, user_name, content) VALUES (4, 1, 2, 2, 31, 'Tom', '好处就是完全不伤胃');
```

## 问题二

```sql
INSERT INTO qa_question(id, clazz_id, user_id, user_name, content) VALUES (2, 1, 30, 'Bob', '找一个靠谱的设计师长期合作？');

INSERT INTO qa_reply(id, question_id, parent_id, top_reply_id, user_id, user_name, content) VALUES (5, 2, 0, 5, 32, 'Max', '明天早上起来加你');
INSERT INTO qa_reply(id, question_id, parent_id, top_reply_id, user_id, user_name, content) VALUES (6, 2, 5, 5, 32, 'Max', '怎么联系');
INSERT INTO qa_reply(id, question_id, parent_id, top_reply_id, user_id, user_name, content) VALUES (7, 2, 0, 7, 32, 'Max', '本人做一些兼职');
```

## 查询问题和回复

只在第一级回复上分页，同时查询出这个回复的所有后代:

1. 查找到问题的 n 个第一级回复的 top_reply_id `x`
2. 查找问题的回复中所有第一级回复为 `x` 的回复

```sql
SELECT q.*, r.*
FROM qa_question q
	JOIN (SELECT question_id, top_reply_id FROM qa_reply WHERE question_id = 1 AND parent_id = 0 LIMIT 0, 10) t ON t.question_id = q.id
	LEFT JOIN qa_reply r ON r.question_id = q.id AND r.top_reply_id = t.top_reply_id
ORDER BY r.created_at DESC;
```

> 修改 question_id 查询不同问题的回复。

## 操作记录

例如查看过问题、点赞、投票、喜欢等行为记录，只记录是否操作过，保存到 qa_action_record 表即可。

问题的查看数量、点赞数量、投票数量、喜欢数量保存到对应的数据条目中。