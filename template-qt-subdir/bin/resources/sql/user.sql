<?xml version="1.0" encoding="UTF-8"?>
<!--
CREATE TABLE user (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username varchar(256) NOT NULL DEFAULT '',
  password varchar(256) NOT NULL DEFAULT '',
  creator  varchar(256) NOT NULL DEFAULT '',
  permission int(11) NOT NULL
) ;

CREATE UNIQUE INDEX IF NOT EXISTS index_user_username ON user (username);
INSERT INTO user(username, password, creator, permission) VALUES('admin', 'admin', 'SUPERMAN', 1);
-->

<sqls namespace="User">
    <define id="fields">id, username, password, creator, permission</define>

    <sql id="findUserById">
        SELECT <include defineId="fields"/> FROM user WHERE id=%1
    </sql>

    <sql id="findUserByUsername">
        SELECT <include defineId="fields"/> FROM user WHERE username=:username
    </sql>

    <sql id="findUserByUsernameAndPassword">
        SELECT <include defineId="fields"/> FROM user WHERE username=:username AND password=:password
    </sql>

    <sql id="findAllUsers">
        SELECT <include defineId="fields"/> FROM user
    </sql>

    <sql id="isUsernameUsed">
        SELECT EXISTS (
            SELECT 1 FROM user WHERE username=:username
        )
    </sql>
	
	<sql id="updatePassword">
		UPDATE user SET password=:password WHERE id=:id
	</sql>

    <sql id="insertUser">
        INSERT INTO user (username, password, creator, permission)
        VALUES (:username, :password, :creator, :permission)
    </sql>

    <sql id="updateUser">
        UPDATE user SET username=:username, password=:password, permission=:permission
        WHERE id=:id
    </sql>

    <sql id="deleteUser">
        DELETE FROM user WHERE id=:id
    </sql>
</sqls>
