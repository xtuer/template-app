package utils

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
)

// ReadFileChunk 读取文件指定范围的内容到 slice bs 中。
func ReadFileChunk(path string, chunkOffset int64, chunkSize uint) (bs []byte, err error) {
	/**
	读取逻辑:
	1. 只读模式打开文件。
	2. 获取文件大小。
	3. 如果 chunkOffset+chunkSize 超过文件范围则进行修正。
	4. 读取 chunk 内容。
	*/

	// [1] 只读模式打开文件。
	file, err := os.Open(path)
	if err != nil {
		return bs, err
	}
	defer file.Close()

	// 起始位置不能小于 0。
	if chunkOffset < 0 {
		return bs, fmt.Errorf("chunkOffset 不能小于 0")
	}

	// [2] 获取文件大小。
	// [3] 如果 chunkOffset+chunkSize 超过文件范围则进行修正。
	stat, err := file.Stat()
	fileSize := stat.Size()
	if chunkOffset >= fileSize {
		return bs, fmt.Errorf("chunkOffset [%d] 超过文件大小 [%d]", chunkOffset, fileSize)
	}
	if chunkOffset+int64(chunkSize) > fileSize {
		chunkSize = uint(fileSize - chunkOffset)
	}

	// [4] 读取 chunk 内容。
	bs = make([]byte, chunkSize)
	_, err = file.ReadAt(bs, chunkOffset)

	return bs, err
}

// ResponseToBean 读取 response body 把其反序列化为对象。
// @param beanPointer 反序列化得到的对象，需要是指针。
func ResponseToBean(rsp *http.Response, beanPointer interface{}) error {
	return json.NewDecoder(rsp.Body).Decode(beanPointer)
}

// GetJson 执行 GET 请求，并把 JSON 字符串的响应体转为对象。
func GetJson(url string, target interface{}, headers ...map[string]interface{}) error {
	var client = &http.Client{}

	if len(headers) > 0 {
		return RequestJson(client, "GET", url, target, nil, "", "", headers[0])
	} else {
		return RequestJson(client, "GET", url, target, nil, "", "")
	}
}

// PostJson 执行 POST 请求，并把 JSON 字符串的响应体转为对象。
func PostJson(url string, target interface{}, requestBody interface{}, headers ...map[string]interface{}) error {
	var client = &http.Client{}

	if len(headers) > 0 {
		return RequestJson(client, "POST", url, target, requestBody, "", ",", headers[0])
	} else {
		return RequestJson(client, "POST", url, target, requestBody, "", "")
	}
}

// PostJson 执行 POST 请求，使用基础认证，并把 JSON 字符串的响应体转为对象。
func PostJsonWithBasicAuth(url string, target interface{}, requestBody interface{}, username string, password string, headers ...map[string]interface{}) error {
	var client = &http.Client{}

	if len(headers) > 0 {
		return RequestJson(client, "POST", url, target, requestBody, username, password, headers[0])
	} else {
		return RequestJson(client, "POST", url, target, requestBody, username, password)
	}
}

// RequestJson 执行 Http 请求，并把 JSON 字符串的响应体转为对象。
// username 和 password 不为 "" 时使用 basic authentication。
func RequestJson(client *http.Client, httpMethod string, url string, target interface{}, requestBody interface{}, username string, password string, headers ...map[string]interface{}) error {
	// 把请求体对象转为 Json 字符串。
	var bodyReader io.Reader
	if requestBody != nil {
		bodyReader = strings.NewReader(ToJson(requestBody))
	}

	// 创建请求。
	req, err := http.NewRequest(httpMethod, url, bodyReader)
	if err != nil {
		return err
	}

	// 设置 header。
	if len(headers) > 0 {
		for name, value := range headers[0] {
			req.Header.Set(name, fmt.Sprintf("%v", value))
		}
	}

	// 使用基础认证。
	if username != "" && password != "" {
		req.SetBasicAuth(username, password)
	}

	// 执行请求。
	rsp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer rsp.Body.Close()

	return json.NewDecoder(rsp.Body).Decode(target)
}

// GetJsonWithBasicAuth 使用基础认证范围 url，请求响应的 Json 格式的数据被反序列化为对象 target。
func GetJsonWithBasicAuth(url, username, password string, target interface{}) error {
	// 使用基础认证方式创建请求。
	client := &http.Client{}
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return err // 例如 url 无效的时候会报错。
	}

	req.SetBasicAuth(username, password)

	// 执行请求。
	rsp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer rsp.Body.Close()

	return json.NewDecoder(rsp.Body).Decode(target)
}

// DownloadFileWithBasicAuth 使用基础认证的方式下载文件。
func DownloadFileWithBasicAuth(url, username, password, path string) error {
	// 使用基础认证方式创建请求。
	client := &http.Client{}
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return err // 例如 url 无效的时候会报错。
	}

	req.SetBasicAuth(username, password)

	// 执行请求。
	rsp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer rsp.Body.Close()

	// 创建文件。
	file, err := os.Create(path)
	if err != nil {
		return err
	}
	defer file.Close()

	// Use io.Copy to copy the response body to the file.
	_, err = io.Copy(file, rsp.Body)
	if err != nil {
		return err
	}

	return nil
}
