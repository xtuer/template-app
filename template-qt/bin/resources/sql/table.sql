DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id       integer primary key autoincrement,
    username text,
    password text,
    email    text,
    mobile   text
);
