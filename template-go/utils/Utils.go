package utils

import (
	"bytes"
	"crypto/md5"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"math"
	"net"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/avast/retry-go"
	"github.com/google/uuid"
)

// Uid 生成唯一 ID。
func Uid() string {
	uid := uuid.NewString()
	uid = strings.ReplaceAll(uid, "-", "")
	return uid
}

// Md5 计算字符串的 MD5 值。
func Md5(text string) string {
	return fmt.Sprintf("%x", md5.Sum([]byte(text)))
}

// Md5OfFile 计算文件的 MD5 值。
func Md5OfFile(filePath string) string {
	file, err := os.Open(filePath)
	if err != nil {
		fmt.Println(err)
		return ""
	}
	hash := md5.New()
	_, _ = io.Copy(hash, file)
	return hex.EncodeToString(hash.Sum(nil))
}

// Md5OfBytes 计算 bytes 数组的 MD5 值。
func Md5OfBytes(bytes []byte) string {
	hash := md5.Sum(bytes)
	return fmt.Sprintf("%x", hash)
}

// FileExist 判断文件或目录是否存在。
func FileExist(path string) bool {
	if _, err := os.Stat(path); !os.IsNotExist(err) {
		return true
	} else {
		return false
	}
}

// ExecuteCommand 执行命令，支持管道。
func ExecuteCommand(command string) (stdoutString string, stderrString string, err error) {
	/**
	逻辑:
	1. 保存命令到临时 shell 文件。
	2. 阻塞执行 shell 文件。
	*/
	var stdout bytes.Buffer
	var stderr bytes.Buffer

	// fmt.Println("执行命令:", command)

	// [1] 保存命令到临时 shell 文件。
	path, err := SaveScriptToTempFile(command, "cmd.sh")
	if err != nil {
		return "", "", err
	}
	// 命令执行结束时删除临时文件。
	defer os.Remove(path)

	// [2] 阻塞执行 shell 文件。
	cmd := exec.Command("sh", "-c", "sh "+path)
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr
	err = cmd.Run()

	return stdout.String(), stderr.String(), err
}

// SaveScriptToTempFile 把脚本内容保存到临时文件，返回脚本的路径。
func SaveScriptToTempFile(scriptContent string, suffix string) (path string, err error) {
	// [1] 创建临时文件。
	tempFile, err := os.CreateTemp("", "newdt-auto-*-"+suffix)
	if err != nil {
		return "", err
	}
	defer tempFile.Close()

	// [2] 把脚本内容写入文件。
	_, err = tempFile.WriteString(scriptContent)
	if err != nil {
		return "", err
	}

	// [3] 把得到的临时文件路径保存到 Job.ScriptPath。
	path = tempFile.Name()

	// [4] 修改脚本的权限为 rwxr-xr-x
	err = os.Chmod(path, 0755)
	if err != nil {
		return path, err
	}

	return path, nil
}

// MergeFiles 合并 files 为一个文件 dstPath。
// 合并成功返回 nil。
func MergeFiles(dstPath string, files []string, targetFileMd5 string) error {
	/**
	逻辑:
	1. 创建要合并的目标文件。
	2. 逐个读取文件内容到目标文件。
	3. 校验合并得到的目标文件的 MD5。
	*/

	// [1] 创建要合并的目标文件。
	out, err := os.Create(dstPath)
	if err != nil {
		return err
	}
	defer out.Close()

	// [2] 逐个读取文件内容到目标文件。
	for _, file := range files {
		f, err := os.Open(file)
		if err != nil {
			return err
		}
		defer f.Close()

		_, err = io.Copy(out, f)
		if err != nil {
			return err
		}
	}

	// [3] 校验合并得到的目标文件的 MD5。
	if targetFileMd5 != Md5OfFile(dstPath) {
		return fmt.Errorf("文件 MD5 不匹配")
	}

	return nil
}

// ToJson 把对象转为 Json 字符串。
func ToJson(obj interface{}) string {
	j, _ := json.Marshal(obj) // j 是 []byte 类型
	return string(j)
}

// FromJson 把 Json 字符串转为对象。
func FromJson(j string, obj interface{}) error {
	return json.Unmarshal([]byte(j), obj)
}

// ProcessExists 判断 pid 的进程是否存在。
// 参考 https://stackoverflow.com/questions/15204162/check-if-a-process-exists-in-go-way
func ProcessExists(pid int) (bool, error) {
	if pid <= 0 {
		return false, fmt.Errorf("invalid pid %v", pid)
	}
	proc, err := os.FindProcess(int(pid))
	if err != nil {
		return false, err
	}
	err = proc.Signal(syscall.Signal(0))
	if err == nil {
		return true, nil
	}
	if err.Error() == "os: process already finished" {
		return false, nil
	}
	errno, ok := err.(syscall.Errno)
	if !ok {
		return false, err
	}
	switch errno {
	case syscall.ESRCH:
		return false, nil
	case syscall.EPERM:
		return true, nil
	}
	return false, err
}

// FormatTime 使用 yyyy-dd-mm hh:mm:ss 的格式格式和字符串。
func FormatTime(t time.Time) string {
	return t.Format("2006-01-02 15:04:05")
}

// HostIps 获取本机的 IPs。
func HostIps() (ips []string) {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return ips
	}

	for _, value := range addrs {
		// 去掉 127.0.0.1: !ipnet.IP.IsLoopback()
		if ipnet, ok := value.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				ips = append(ips, ipnet.IP.String())
			}
		}
	}

	return ips
}

// ExecDir 获取可执行程序的目录。
func ExecDir() string {
	execPath, _ := os.Executable()
	execPath = strings.Replace(execPath, "\\", "/", -1)
	execDir := filepath.Dir(execPath)

	return execDir
}

// ExecName 获取进程的文件名。
func ExecName() string {
	execPath, _ := os.Executable()
	execName := filepath.Base(execPath)

	return execName
}

// SavePid 保存 PID 到文件。
func SavePid(path string) {
	pid := os.Getpid()
	content := fmt.Sprintf("%d", pid)

	os.WriteFile(path, []byte(content), 0644)
}

// CompareVersion 比较版本，版本的格式为 ^v\d+(\.\d+)*$，以 v 开头，数字间以英文句号分隔。
// 逐个按照数字部分大小进行比较，直到第一个不相等。
// v1 > v2 返回正数。
// v1 = v2 返回 0。
// v1 < v2 返回负数。
// v1, v2 不符合版本格式 err 为非 nil。
//
// 示例:
// CompareVersion("v0.8.21", "v0.8.10") 返回 11, nil
// CompareVersion("v0.9.21", "v0.8") 返回 1, nil
// CompareVersion("v0.9", "v0.8.21") 返回 1, nil
func CompareVersion(v1, v2 string) (int, error) {
	/**
	逻辑:
	1. 版本格式校验。
	2. 如果 v1 == v2 则返回 0。
	3. 去掉版本前面的 v，然后按照 . 进行分隔，得到数值的 slice。
	4. 逐个比较 slice 中的元素，返回第一个不相等的元素差。
	*/

	// [1] 版本格式校验。
	if ok, _ := CheckVersion(v1); !ok {
		return 0, fmt.Errorf("v1 格式不对")
	}
	if ok, _ := CheckVersion(v2); !ok {
		return 0, fmt.Errorf("v2 格式不对")
	}

	// [2] 如果 v1 == v2 则返回 0。
	if v1 == v2 {
		return 0, nil
	}

	// [3] 去掉版本前面的 v，然后按照 . 进行分隔，得到数值 slice。
	v1 = v1[1:]
	v2 = v2[1:]
	ns1 := []int{}
	ns2 := []int{}

	for _, t := range strings.Split(v1, ".") {
		n, _ := strconv.Atoi(t)
		ns1 = append(ns1, n)
	}
	for _, t := range strings.Split(v2, ".") {
		n, _ := strconv.Atoi(t)
		ns2 = append(ns2, n)
	}

	// [4] 逐个比较 slice 中的元素，返回第一个不相等的元素差。
	len1 := len(ns1)
	len2 := len(ns2)
	comm := int(math.Min(float64(len1), float64(len2)))
	for i := 0; i < comm; i++ {
		if ns1[i] == ns2[i] {
			continue
		}

		return ns1[i] - ns2[i], nil
	}

	return len1 - len2, nil
}

// CheckVersion 校验版本格式。
func CheckVersion(v string) (ok bool, err error) {
	// [1] 版本格式校验。
	pattern := `^v\d+(\.\d+)*$`
	return regexp.MatchString(pattern, v)
}

// Retry 尝试执行函数 fn 最多 times 次，每次尝试之间间隔 delay，第一次执函数 fn 未成功继续尝试时执行 onRetry 函数。
func Retry(times uint, delay time.Duration, fn func() error, onRetry ...func(n uint, err error)) error {
	return retry.Do(
		fn,
		retry.Attempts(times),
		retry.Delay(delay), // retry 之间的 delay，不包含逻辑执行时间
		retry.DelayType(retry.FixedDelay),
		retry.OnRetry(func(n uint, err error) {
			if len(onRetry) > 0 {
				onRetry[0](n, err)
			}
		}),
	)
}

// Sign 计算签名。
//
// @param secret 签名密钥。
// @param salt 签名的盐。
// @param signAt 签名的时间，单位为秒。
// @return 返回签名字符串。
func Sign(secret, salt string, signAt int64) string {
	return Md5(fmt.Sprintf("%d%s", signAt, Md5(secret+salt)))
}

// IsFileWritable 判断文件是否可写。
func IsFileWritable(filepath string) bool {
	file, err := os.OpenFile(filepath, os.O_WRONLY, 0666)
	if err != nil {
		if os.IsPermission(err) {
			return false
		}
	}
	file.Close()
	return true
}

// ListFileNamesInDir 获取传入的目录 dir 中的文件名。
func ListFileNamesInDir(dir string) (fileNames []string, err error) {
	files, err := ioutil.ReadDir(dir)
	if err != nil {
		return fileNames, err
	}

	for _, file := range files {
		if !file.IsDir() {
			name := file.Name()
			if !strings.HasPrefix(name, ".") {
				fileNames = append(fileNames, name)
			}
		}
	}

	return fileNames, nil
}

// ReadTextInRange 读取传入文件 path 在行 [startLine, endLine] 之间的内容。
func ReadTextInRange(path string, startLine, endLine uint) (text string, err error) {
	/**
	逻辑:
	1. 拼接出获取文件指定行之间内容的命令，如s: sed -n '10,20p' test.txt
	2. 执行命令。
	*/

	cmd := fmt.Sprintf("sed -n '%d,%dp' %s", startLine, endLine, path)

	text, _, err = ExecuteCommand(cmd)
	if err != nil {
		return "", err
	} else {
		return text, nil
	}
}

// IsPortUsed 检查端口是否被占用。
func IsPortUsed(port uint) bool {
	// Attempt to establish a connection to the port.
	conn, err := net.DialTimeout("tcp", fmt.Sprintf("localhost:%d", port), time.Second)
	if err == nil {
		conn.Close()
		return true
	} else {
		return false
	}
}
