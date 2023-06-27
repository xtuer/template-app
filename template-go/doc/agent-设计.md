## 数据结构设计

Job, JobState (JSRunning), JobStore, ErrorCode (ECJobNotFound)，Response。

每个执行的任务创建一个 Job 对象，保存到 JobStore。

请求响应统一格式:

```go
type Response struct {
  Success bool `json:"success"`
  Code int 		 `json:"code"`
  Msg string 	 `json:"msg"`
  Data any 		 `json:"data"`
}
```

## 执行命令

URI: `/api/jobs/execute/cmd`
HTTP Method: `POST`

| 参数名 | 类型   | 说明                           |
| ------ | ------ | ------------------------------ |
| cmd    | string | 要执行的命令                   |
| params | string | 命令和脚本的参数 -k1 v1 -k2 v2 |
| async  | bool   | 是否异步执行                   |

请求响应的关键字段:

```json
{
  "success": true,
  "code": 0, // 非 0 为错误
  "msg": "xxx",
  "data": {
    "id": "<uuid>",     // JobID
    "pid": 123,         // 进程 PID
    "state": "running", // running, finished, canceled
    "stdout": "xxx",    // 标准输出
    "stderr": "xxx",    // 错误输出
    "rc": 0,            // 进程的 return code
    "async": true       // 是否异步执行
  }
}
```

提示: 简单命令使用阻塞模式，耗时命令和脚本使用异步模式。`data` 部分的完整字段如下:

```go
type Job struct {
	Id            string     `json:"id"`            // 任务的 ID，使用 uuid 生成
	Cmd           string     `json:"cmd"`           // 要执行的命令
	Params        string     `json:"params"`        // 执行命令或者脚本的参数，格式为 -k1 v1 -k2 v2
	ScriptName    string     `json:"scriptName"`    // 要执行的脚本名称
	ScriptContent string     `json:"scriptContent"` // 脚本内容
	ScriptType    ScriptType `json:"scriptType"`    // 脚本类型，值为 shell 或者 python
	ScriptPath    string     `json:"scriptPath"`    // 脚本内容保存到系统上的路径
	Async         bool       `json:"async"`         // 是否异步执行命令或者脚本，为 true 时异步执行，为 false 时同步执行

	Pid      int      `json:"pid"`      // 任务的进程的 PID
	State    JobState `json:"state"`    // 任务的状态，值为 running, finished, canceled
	Stdout   string   `json:"stdout"`   // 任务的进程的标准输出
	Stderr   string   `json:"stderr"`   // 任务的进程的错误输出
	ExitCode int      `json:"exitCode"` // 任务的进程的返回值
	ExecCmd  string   `json:"execCmd"`  // 任务执行的命令
	Error    string   `json:"error"`    // Error msg when fork & exec

	CreateTime time.Time `json:"createTime"` // 任务创建的时间
	StartTime  time.Time `json:"startTime"`  // 任务开始的时间
	FinishTime time.Time `json:"finishTime"` // 任务结束的时间
}
```

## 执行脚本

> Agent 接收到执行脚本类型的请求:
>
> 1. 把 scriptContent 作为 shell 脚本保存到文件系统，例如得到 `/var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-102948235-aloha.sh`。
> 2. 再调用命令 `sh -c 'bash /var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-102948235-aloha.sh -k1 "v1" -k2 "v2"'` 。

URI: `/api/jobs/execute/script`
HTTP Method: `POST`

| 参数名        | 类型   | 说明                             |
| ------------- | ------ | -------------------------------- |
| scriptName    | string | 脚本名称                         |
| scriptContent | string | 脚本内容，Agent 会保存到文件系统 |
| scriptType    | string | 脚本类型 (shell 或 python)       |
| params        | string | 命令和脚本的参数 -k1 v1 -k2 v2   |
| async         | bool   | 是否异步执行                     |

请求响应: ==参考执行命令==

## 任务信息

URI: `/api/jobs/<uuid>`
HTTP Method: `GET`

请求响应: ==参考执行命令==

## 鉴权验证

请求需要鉴权通过才允许访问，可以采用无状态的签名算法进行验证。

**生成签名:**

* 获取当前时间 signAt (秒)。

* 计算签名: `md5(signAt + md5(<secret> + <salt>))`。

* sign 和 signAt:
  * 可以放到 URL Query 参数里

  * 也可以放到 header 里

    ```shell
    curl 'http://localhost:8080/api/jobs/21bfd5a230394b9b963441b94b7d2f51?sign=eeae86cbf9b42a8be7fdee31e9aabb13&signAt=1671084695'

    curl 'http://localhost:8080/api/jobs/21bfd5a230394b9b963441b94b7d2f51' -H "sign: eeae86cbf9b42a8be7fdee31e9aabb13" -H "signAt: 1671084695"
    ```

* 服务器和客户段都需要设置配对密钥。

**签名验证:**

    1. 获取请求的路径。
    2. 如果请求路径在白名单中，不需要鉴权验证。
    3. 获取签名的 sign 和 signAt 信息:
       3.1 如果能从 URL Query 中获取签名信息则从 URL Query 中获取。
       3.2 如果能从 Header 中获取签名信息则从 Header 中获取。
       3.3 如果获取不到签名信息则终止请求。
    4. 验证签名，签名无效者终止请求。
          * 签名需要验时效性 (30m)，也可以每次调用都现生成，否则一个签名一直使用会有问题。
    5. 验证通过，请求继续。

**Mac 下生成签名的脚本:**

```shell
#!/bin/bash

secret="shindata"
signAt=$(date +%s)
one=$(echo -n "${secret}newdt" | md5 | xargs)
sign=$(echo -n "${signAt}${one}" | md5 | xargs)

echo "sign=$sign&signAt=$signAt"
echo "-H 'sign: $sign' -H 'signAt: $signAt'"
```

**Linux 下生成签名的脚本:**

```shell
#!/bin/bash

secret="shindata"
signAt=$(date +%s)
one=$(echo -n "${secret}newdt" | md5sum | sed 's/ .*$//')
sign=$(echo -n "${signAt}${one}" | md5sum | sed 's/ .*$//')

echo "sign=$sign&signAt=$signAt"
echo "-H 'sign: $sign' -H 'signAt: $signAt'"
```
