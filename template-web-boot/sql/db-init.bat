@echo off

echo "作用: 导入当前目录下的所有 SQL 文件初始化数据库"

rem 需要三个参数，参数不对时退出
if "%3%"=="" (
    echo "命令: db-init.bat db_username db_password db_name"
    goto End
)

set username=%1
set password=%2
set dababase=%3

rem 遍历当前文件夹下的所有 SQL 文件，使用 mysql 命令导入数据库
rem mysql -uroot -proot training < demo.sql

for /f "delims=" %%f in ('dir /b /a-d /s "*.sql"') do (
    echo %%f
    call mysql -u%username% -p%password% %dababase% < %%f
)

:End
