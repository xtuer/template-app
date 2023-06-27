## 签名生成

请求需要认证通过才允许执行，执行下面的脚本生成签名。

Linux:

```shell
#!/bin/bash

secret="shindata"
signAt=$(date +%s)
one=$(echo -n "${secret}newdt" | md5sum | sed 's/ .*$//')
sign=$(echo -n "${signAt}${one}" | md5sum | sed 's/ .*$//')

echo "sign=$sign&signAt=$signAt"
echo "-H 'sign: $sign' -H 'signAt: $signAt'"
```

Mac:

```shell
#!/bin/bash

secret="shindata"
signAt=$(date +%s)
one=$(echo -n "${secret}newdt" | md5 | xargs)
sign=$(echo -n "${signAt}${one}" | md5 | xargs)

echo "sign=$sign&signAt=$signAt"
echo "-H 'sign: $sign' -H 'signAt: $signAt'"
```

使用 Curl 发起请求时，可以把签名放在 URL Query 中或者 Header 里:

```shell
curl http://localhost:8080/api/jobs/6dc638d636124a8ea08e1bbe204915d0?sign=1375db7c38acd9300ea67b5351956c3b&signAt=1671170774

curl http://localhost:8080/api/jobs/6dc638d636124a8ea08e1bbe204915d0 -H 'sign: 1375db7c38acd9300ea67b5351956c3b' -H 'signAt: 1671170774'
```

> 注意:
>
> * 下面的测试用例，每一个都要带上这里生成的认证信息，因为是公共的，且为了文档简洁在介绍时没有带上，实际使用时自行带上。
> * 签名有实效性，目前是 30 分钟，记得更新。

## 任务状态

```go
// JobState 为任务状态。
type JobState string

const (
	JSRunning  JobState = "running"  // 执行中
	JSSuccess  JobState = "success"  // 执行成功
	JSFailed   JobState = "failed"   // 执行失败
	JSCanceled JobState = "canceled" // 被取消
	JSRejected JobState = "rejected" // 拒绝执行
)
```

> 注意: 当命令或者脚本的 exit code 为非 0 时任务执行失败，状态为 failed。

## 响应说明

响应的几个主要字段:

* id: 执行命令或者脚本都会生成一个任务，每个任务都有一个唯一的 ID，异步执行的时候使用这个 ID 去查询任务的状态
* state: 任务的状态，参考上面`任务状态`说明
* pid: 任务的进程 PID
* stdout: 任务的正常输出
* stderr: 任务的错误输出
* exitCode: 任务进程退出时的 code
* createTime: 创建任务的时间
* startTime: 开始执行任务的时间
* finishtime: 任务结束时间 (正常结束、异常结束)

## 执行命令

命令执行的请求主要参数为:

* cmd: 要执行的命令
* async: true 为异步执行，false 为同步执行

### 同步执行命令 (async 为 false):

1. 使用 Curl 发起请求:

   ```shell
   curl -X POST 'http://localhost:8080/api/jobs/execute/cmd' -H 'Content-Type: application/json' -d '{"cmd": "for ((i=0;i<10;i++));do echo $i; sleep 1; done;", "async": false}'
   ```

2. 等待 10 秒，得到响应:

   ```json
   {
       "code": 0,
       "success": true,
       "msg": "",
       "data": {
           "id": "24031b9e842a435d86562f771a7992f1",
           "cmd": "for ((i=0;i\u003c10;i++));do echo $i; sleep 1; done;",
           "params": "",
           "scriptName": "",
           "scriptContent": "",
           "scriptType": "",
           "scriptPath": "",
           "async": false,
           "pid": 7771,
           "state": "success",
           "stdout": "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n",
           "stderr": "",
           "exitCode": 0,
           "execCmd": "for ((i=0;i\u003c10;i++));do echo $i; sleep 1; done;",
           "error": "",
           "createTime": "2022-12-14T14:31:10.748369+08:00",
           "startTime": "2022-12-14T14:31:10.748966+08:00",
           "finishTime": "2022-12-14T14:31:20.875891+08:00"
       }
   }
   ```

   其中 stdout 为命令的输出。

### 异步执行命令 (async 为 true):

1. 使用 Curl 发起请求:

   ```shell
   curl -X POST 'http://localhost:8080/api/jobs/execute/cmd' -H 'Content-Type: application/json' -d '{"cmd": "for ((i=0;i<50;i++));do echo $i; sleep 1; done;", "async": true}'
   ```

   把 for 循环中的次数修改为 50，这样让任务执行时间长一点，方便验证查询异步任务的状态。

2. 查询任务状态:

   ```shell
   curl http://localhost:8080/api/jobs/603883de4a9545b3ab1fe80c40c9de90
   ```

   `jobs/` 后面为任务 ID。

3. 得到任务状态 (注意 state 值，任务还在执行则为 running，成功为 success，失败为 failed (例如 kill 掉进程)):

   ```json
   {
       "code": 0,
       "success": true,
       "msg": "",
       "data": {
           "id": "d8f0bebdfa1d4d26a06bf62cb29dd11b",
           "cmd": "for ((i=0;i\u003c50;i++));do echo $i; sleep 1; done;",
           "params": "",
           "scriptName": "",
           "scriptContent": "",
           "scriptType": "",
           "scriptPath": "",
           "async": true,
           "pid": 8102,
           "state": "running",
           "stdout": "",
           "stderr": "",
           "exitCode": 0,
           "execCmd": "for ((i=0;i\u003c50;i++));do echo $i; sleep 1; done;",
           "error": "",
           "createTime": "2022-12-14T14:42:07.752221+08:00",
           "startTime": "2022-12-14T14:42:07.752322+08:00",
           "finishTime": "0001-01-01T00:00:00Z"
       }
   }
   
   {
       "code": 0,
       "success": true,
       "msg": "",
       "data": {
           "id": "d8f0bebdfa1d4d26a06bf62cb29dd11b",
           "cmd": "for ((i=0;i\u003c50;i++));do echo $i; sleep 1; done;",
           "params": "",
           "scriptName": "",
           "scriptContent": "",
           "scriptType": "",
           "scriptPath": "",
           "async": true,
           "pid": 8102,
           "state": "success",
           "stdout": "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17\n18\n19\n20\n21\n22\n23\n24\n25\n26\n27\n28\n29\n30\n31\n32\n33\n34\n35\n36\n37\n38\n39\n40\n41\n42\n43\n44\n45\n46\n47\n48\n49\n",
           "stderr": "",
           "exitCode": 0,
           "execCmd": "for ((i=0;i\u003c50;i++));do echo $i; sleep 1; done;",
           "error": "",
           "createTime": "2022-12-14T14:42:07.752221+08:00",
           "startTime": "2022-12-14T14:42:07.752322+08:00",
           "finishTime": "2022-12-14T14:42:58.37255+08:00"
       }
   }
   ```

## 执行脚本

脚本执行的请求参数为:

* cmd: 为空
* scriptName: 脚本名称，例如 x.sh
* scriptContent: 脚本内容 (Agent 会把脚本内容保存到临时文件)
* scriptType: 脚本类型，可选址为 shell 或者 python
* async: true 为异步执行，false 为同步执行 (一般脚本都推荐使用异步执行)

同步脚本执行和异步脚本执行的测试方式和上面测试执行命令的流程一样。

### 同步执行脚本 (async 为 false):

1. 使用 Curl 发起请求:

   ```shell
   curl -X POST 'http://localhost:8080/api/jobs/execute/script' -H 'Content-Type: application/json' -d '{"scriptName": "x.sh", "scriptContent": "for ((i=0;i<10;i++));do echo $i; sleep 1; done;", "scriptType": "shell", "async": false }'
   ```

2. 等待 10 秒，得到响应:

   ```json
   {
       "code": 0,
       "success": true,
       "msg": "",
       "data": {
           "id": "7474cd8662794bd6906950943de6d560",
           "cmd": "",
           "params": "",
           "scriptName": "x.sh",
           "scriptContent": "",
           "scriptType": "shell",
           "scriptPath": "/var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-4230492039-x.sh",
           "async": false,
           "pid": 8485,
           "state": "success",
           "stdout": "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n",
           "stderr": "",
           "exitCode": 0,
           "execCmd": "bash /var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-4230492039-x.sh ",
           "error": "",
           "createTime": "2022-12-14T14:55:32.42791+08:00",
           "startTime": "2022-12-14T14:55:32.428167+08:00",
           "finishTime": "2022-12-14T14:55:42.558224+08:00"
       }
   }
   ```
   

异步执行脚本测试和上面异步执行命令一样的操作，就不在列举了。

## 执行失败

任务执行失败的时候 state 为 failed，一般有 2 种情况导致任务执行失败:

* 脚本或命令的 exit code 为非 0 值
* 任务的进程被 kill

需要注意点是进程的 exit code 为 0 并不代表是执行成功，不能简单的使用 exit code 判断是否执行成功:

* 进程正常结束时 exit code 为 0 表示执行成功，非 0 表示执行失败
* 进程被 kill 掉，进程的 exit code 有时候时 0

### 脚本或命令的 exit code 为非 0 值

1. 使用 Curl 执行脚本，脚本中增加非 0 退出值 `exit 123`:

   ```shell
   curl -X POST 'http://localhost:8080/api/jobs/execute/script' -H 'Content-Type: application/json' -d '{"scriptName": "x.sh", "scriptContent": "for ((i=0;i<10;i++));do echo $i; sleep 1; done; exit 123", "scriptType": "shell", "async": false }'
   ```

2. 等待响应，state 为 failed:

   ```json
   {
       "code": 0,
       "success": true,
       "msg": "",
       "data": {
           "id": "a5b4508a9c6945bfae6f8f6e055e1b33",
           "cmd": "",
           "params": "",
           "scriptName": "x.sh",
           "scriptContent": "",
           "scriptType": "shell",
           "scriptPath": "/var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-2291141166-x.sh",
           "async": false,
           "pid": 8602,
           "state": "failed",
           "stdout": "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n",
           "stderr": "",
           "exitCode": 123,
           "execCmd": "bash /var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-2291141166-x.sh ",
           "error": "exit status 123",
           "createTime": "2022-12-14T15:00:18.961642+08:00",
           "startTime": "2022-12-14T15:00:18.961878+08:00",
           "finishTime": "2022-12-14T15:00:29.096603+08:00"
       }
   }
   ```

### 任务的进程被 kill

1. 使用 Curl 执行脚本:

   ```shell
   curl -X POST 'http://localhost:8080/api/jobs/execute/script' -H 'Content-Type: application/json' -d '{"scriptName": "x.sh", "scriptContent": "for ((i=0;i<50;i++));do echo $i; sleep 1; done;", "scriptType": "shell", "async": true }'
   ```

2. 找到任务的进程的 PID 并 kill 掉任务进程: `kill 8715` (可以看到 Agent 的日志里任务也结束了)

3. 查询任务状态:

   ```shell
   curl http://localhost:8080/api/jobs/cb0da1f8867c49a498f1d38c77479442
   ```

   ```json
   {
       "code": 0,
       "success": true,
       "msg": "",
       "data": {
           "id": "cb0da1f8867c49a498f1d38c77479442",
           "cmd": "",
           "params": "",
           "scriptName": "x.sh",
           "scriptContent": "",
           "scriptType": "shell",
           "scriptPath": "/var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-2572570948-x.sh",
           "async": true,
           "pid": 8715,
           "state": "failed",
           "stdout": "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17\n18\n19\n20\n21\n22\n23\n",
           "stderr": "",
           "exitCode": 0,
           "execCmd": "bash /var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-2572570948-x.sh ",
           "error": "signal: terminated",
           "createTime": "2022-12-14T15:04:52.78497+08:00",
           "startTime": "2022-12-14T15:04:52.785629+08:00",
           "finishTime": "2022-12-14T15:05:17.081451+08:00"
       }
   }
   ```

   可以看到 state 为 failed，进程不是正常结束的，所以任务执行失败。

## 任务数量限制

   目前一个 Agent 允许最多有 5 个任务同时执行，再提交新的任务会被拒绝。

   1. 提交下面的任务 5 次:

      ```shell
      curl -X POST 'http://localhost:8080/api/jobs/execute/script' -H 'Content-Type: application/json' -d '{"scriptName": "x.sh", "scriptContent": "for ((i=0;i<50;i++));do echo $i; sleep 1; done; exit 123", "scriptType": "shell", "async": true }'
      ```

   2. 第 6 次提交得到响应任务被拒绝:

      ```json
      {
          "id":"92251e1986ab41eb860e4f37c1a8450e",
          "code":1,
          "success":false,
          "msg":"有 5 个任务正在执行，不能执行新任务，请稍后再尝试"
      }
      ```

   3. 查看当前正在执行的任务数量:

      ```shell
      curl http://localhost:8080/api/jobs/countOfRunningJobs
      ```

      ```json
      {
          "code": 0,
          "success": true,
          "msg": "",
          "data": 2
      }
      ```

   4. 稍等一会正在执行的任务数量小于 5 时再次提交任务就可以继续提交了。


## 退出 Agent

   退出 Agent 后，由 Agent 创建的正在执行的任务的进程也会被自动结束掉。

   1. 提交下面的任务 3 次:

      ```shell
      curl -X POST 'http://localhost:8080/api/jobs/execute/script' -H 'Content-Type: application/json' -d '{"scriptName": "x.sh", "scriptContent": "for ((i=0;i<100;i++));do echo $i; sleep 1; done;", "scriptType": "shell", "async": true }'
      ```

   2. 查看任务的进程: `ps -ef | grep x.sh`

      ```shell
      bash /var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-1686790369-x.sh
      bash /var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-3389339783-x.sh
      bash /var/folders/b_/l6lhwt755r10s89w1ss33ly80000gn/T/newdt-auto-29564953-x.sh
      ```

   3. 结束 Agent 进程

   4. 查看任务的进程: `ps -ef | grep x.sh`:

      ```shell
      相关任务进程全没了
      ```

      