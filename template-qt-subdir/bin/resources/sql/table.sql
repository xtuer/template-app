# 用户表，默认创建了超级管理员

DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username varchar(256) NOT NULL DEFAULT '',
  password varchar(256) NOT NULL DEFAULT '',
  creator  varchar(256) NOT NULL DEFAULT '',
  permission int(11) NOT NULL
) ;

CREATE UNIQUE INDEX IF NOT EXISTS index_user_username ON user (username);
INSERT INTO user(username, password, creator, permission) VALUES('admin', 'admin', 'SUPERMAN', 1);

# 报表向导设置
DROP TABLE IF EXISTS report_settings;
CREATE TABLE report_settings (
    id integer primary key autoincrement,
    device_type integer unique,
    device_type_name text,
    settings text
);

INSERT OR REPLACE INTO report_settings (device_type, device_type_name, settings) VALUES (1, '蒸汽灭菌设备', '{yyyy}');
