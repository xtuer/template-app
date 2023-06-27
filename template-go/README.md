Go 实现的程序。

## 开发运行
开发环境下运行，其中 wd 的值为项目所在目录。

```shell
go run main.go -wd /Users/biao/Documents/workspace/template-app/template-go
```

## Aix 编译

Aix Golang 1.14 版本，需要调整目录结构，把 vendor 下的第三方库放到我们项目同一个父目录中，例如

```shell
src
├── github.com
├── golang.org
├── google.golang.org
├── gopkg.in
├── modules.txt
├── template-go
└── sigs.k8s.io
```

