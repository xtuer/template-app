package utils

import (
	"crypto/md5"
	"fmt"
	"os"

	"github.com/google/uuid"
)

// Uid 生成唯一 ID。
func Uid() string {
	return uuid.NewString()
}

// Md5 计算字符串的 MD5 值。
func Md5(text string) string {
	return fmt.Sprintf("%x", md5.Sum([]byte(text)))
}

// SaveScriptToTempFile 把脚本内容保存到临时文件，返回脚本的路径。
func SaveScriptToTempFile(scriptContent string) (path string, err error) {
	// [1] 创建临时文件。
	tempFile, err := os.CreateTemp("", "ndt-auto-*.sh")
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

	return path, nil
}
