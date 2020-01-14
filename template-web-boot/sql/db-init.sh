#!/bin/bash

echo "作用: 导入当前目录下的所有 SQL 文件初始化数据库"

# 需要三个参数，参数不对时退出
if [ $# != 3 ] ; then
    echo "命令: sh db-init.sh db_username db_password db_name"
    exit 1;
fi

username=$1
password=$2
database=$3

# 遍历当前文件夹下的所有 SQL 文件，使用 mysql 命令导入数据库
# mysql -uroot -proot training < demo.sql

for sqlFile in ./*.sql
do
    if test -f $sqlFile
    then
        echo "$sqlFile"
        `mysql -u$username -p$password $database < $sqlFile`
    fi
done
