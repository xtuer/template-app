#!/bin/bash

# 脚本执行:
# 1. 执行脚本 sh build.sh v1.2
# 2. 选择要编译的程序

# 编译 newdt-agent:
# 使用示例:
# - buildAgent linux amd64 newdt-agent main.go v1.2
# - buildAgent linux amd64 newdt-agent Watchdog.go v1.2
#
# 参数:
# $1: 系统类型，如 linux, windows
# $2: 架构类型，如 amd64, arm64
# $3: 编译出来的可执行程序名称，如 newdt-agent, newdt-agent.exe
# $4: go 的入口文件名，如 main.go, Watchdog.go
function buildAgent {
    GOOS=$1
    GOARCH=$2
    EXEC_DIR="bin/newdt-agent-$GOOS-$GOARCH"
    EXEC_PATH="bin/newdt-agent-$GOOS-$GOARCH/$3"
    SOURCE_ENTRY_FILE=$4
    VERSION=$5
    EXEC_PATH_WITH_VERSION="bin/newdt-agent-$GOOS-$GOARCH/$3_${VERSION}_${GOOS}_${GOARCH}"

    if [[ -z "${VERSION}" ]]; then
        echo "Version is empty"
        exit 500
    fi

    # 创建可执行文件目录。
    $(mkdir -p $EXEC_DIR)

    # 编译。
    $(CGO_ENABLED=0 GOOS=$GOOS GOARCH=$GOARCH go build -ldflags="-s -w" -o $EXEC_PATH $SOURCE_ENTRY_FILE)
    cp ${EXEC_PATH} ${EXEC_PATH_WITH_VERSION}

    # 编译结果，非 0 为失败。
    buildResult=$?
    [ $buildResult -eq 0 ] && echo "编译结果: $EXEC_PATH" || echo "编译失败"
}

VERSION=$1
COLUMNS=0
PS3='Please enter your choice: '
options=(
    "编译 newdt-agent: Linux amd64"
    "编译 newdt-agent: Linux arm64"
    "编译 newdt-agent: Aix ppc64"
    "编译 newdt-watchdog: Linux amd64"
    "编译 newdt-watchdog: Linux arm64"
    "编译 newdt-watchdog: Aix ppc64"
    "Quit"
)

select opt in "${options[@]}"
do
    case $opt in
        ${options[0]})
            buildAgent linux amd64 newdt-agent Agent.go ${VERSION}
            break
            ;;
        ${options[1]})
            buildAgent linux arm64 newdt-agent Agent.go ${VERSION}
            break
            ;;
        ${options[2]})
            buildAgent aix ppc64 newdt-agent Agent.go ${VERSION}
            break
            ;;

        ${options[3]})
            buildAgent linux amd64 newdt-watchdog Watchdog.go ${VERSION}
            break
            ;;
        ${options[4]})
            buildAgent linux arm64 newdt-watchdog Watchdog.go ${VERSION}
            break
            ;;
        ${options[5]})
            buildAgent aix ppc64 newdt-watchdog Watchdog.go ${VERSION}
            break
            ;;
        "Quit")
            exit
            ;;
        *) echo "Invalid option $REPLY";;
    esac
done
