package utils

import (
	"fmt"
	"regexp"
	"strings"
	"testing"
)

func TestUid(t *testing.T) {
	fmt.Println(Uid())
}

func TestMd5(t *testing.T) {
	fmt.Println(Md5("Hello"))
}

func TestSaveScriptToTempFile(t *testing.T) {
	path, _ := SaveScriptToTempFile("Test only content", "x.sh")
	fmt.Println(path)
}

func TestFileMd5(t *testing.T) {
	md5 := Md5OfFile("/Users/biao/Documents/workspace/newdt/ndtagent_http/upload-tmp/abcd-1.tmp")
	fmt.Println(md5)
}

func TestBigFileMd5(t *testing.T) {
	md5 := Md5OfFile("/Users/biao/Documents/workspace/win11-arm.iso")
	fmt.Println(md5)
}

func TestFileExist(t *testing.T) {
	fmt.Println(FileExist("/Users/biao/Desktop/x"))
}

// 测试合并文件
// 分割文件名了: split -b 1m "gotop" "gotop."
func TestMergeFiles(t *testing.T) {
	files := []string{
		"/Users/biao/Desktop/x/x.pdf.aa",
		"/Users/biao/Desktop/x/x.pdf.ab",
		"/Users/biao/Desktop/x/x.pdf.ac",
		"/Users/biao/Desktop/x/x.pdf.ad",
		"/Users/biao/Desktop/x/x.pdf.ae",
		"/Users/biao/Desktop/x/x.pdf.af",
		"/Users/biao/Desktop/x/x.pdf.ag",
		"/Users/biao/Desktop/x/x.pdf.ah",
		"/Users/biao/Desktop/x/x.pdf.ai",
	}
	err := MergeFiles("/Users/biao/Desktop/x/1.pdf", files, "6909b911bbd8e06436a580530de96ae8")
	fmt.Println(err)
}

func TestGetAgentMd5(t *testing.T) {
	url := "http://localhost:8290/api/agents/versions/v1.0.12a/md5InPartition"
	result := map[string]interface{}{}
	GetJsonWithBasicAuth(url, "newdt", "newdt", &result)
	status := int(result["status"].(float64))
	msg := result["msg"].(string)
	fmt.Println(status, msg)
}

func TestCountFileLines(t *testing.T) {
	stdout, _, _ := ExecuteCommand("wc -l /Users/biao/Documents/workspace/newdt/newdtagent/logs/*")
	lines := strings.Split(stdout, "\n")

	re := regexp.MustCompile(`\s*(\d+).+/(.+\.log)$`)
	for _, line := range lines {
		// fmt.Println(line)

		match := re.FindStringSubmatch(line)
		if match != nil {
			fmt.Println(match[1], match[2])
		}
	}
}
