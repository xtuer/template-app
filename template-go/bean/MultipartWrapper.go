package bean

import (
	"bytes"
	"fmt"
	"io"
	"mime/multipart"
	"os"
	"xtuer/utils"
)

// MultipartWrapper 为传文件使用的 multipart 封装。
// 使用方法:
// 1. 创建实例: mw := bean.NewMultipartWrapper()
// 2. 添加文件: mw.AddFileChunk("/Users/biao/Downloads/chunks/arthas.zip", 10000000, 20000000)
// 3. 添加参数: mw.AddFormField("sn", 1)
// 4. 关闭写入: mw.Close()
// 5. 创建请求: req, err := http.NewRequest("POST", url, mw.BodyReader())
// 6. 设置 Header: req.Header.Set("Content-Type", mw.ContentType())
type MultipartWrapper struct {
	bodyBuffer *bytes.Buffer
	bodyWriter *multipart.Writer
}

// NewMultipartWrapper 创建 MultipartWrapper 对象。
func NewMultipartWrapper() *MultipartWrapper {
	writer := &MultipartWrapper{}
	writer.bodyBuffer = &bytes.Buffer{}
	writer.bodyWriter = multipart.NewWriter(writer.bodyBuffer)

	return writer
}

// AddFileChunk 添加上传的分片。
func (my *MultipartWrapper) AddFileChunk(path string, chunkOffset int64, chunkSize uint) (chunkMd5 string, err error) {
	chunkContent, err := utils.ReadFileChunk(path, chunkOffset, chunkSize)
	if err != nil {
		return "", err
	}

	fileWriter, err := my.bodyWriter.CreateFormFile("file", "chunk-x")
	if err != nil {
		return "", err
	}
	_, err = fileWriter.Write([]byte(chunkContent))
	if err != nil {
		return "", err
	}
	chunkMd5 = utils.Md5OfBytes(chunkContent)

	return chunkMd5, nil
}

// AddFile 添加文件。
func (my *MultipartWrapper) AddFile(path string) error {
	f, err := os.Open(path)
	if err != nil {
		fmt.Println("error opening file")
		return err
	}
	defer f.Close()

	fi, _ := f.Stat()
	fileWriter, err := my.bodyWriter.CreateFormFile("file", fi.Name())
	if err != nil {
		return err
	}
	_, err = io.Copy(fileWriter, f)

	return err
}

// AddFormField 添加表单项。
func (my *MultipartWrapper) AddFormField(name string, value interface{}) error {
	fieldWriter, err := my.bodyWriter.CreateFormField(name)
	if err != nil {
		return err
	}
	_, err = fieldWriter.Write([]byte(fmt.Sprintf("%v", value)))
	if err != nil {
		return err
	}

	return nil
}

// AddFormFields 添加表单项。
func (my *MultipartWrapper) AddFormFields(fields map[string]interface{}) error {
	for name, value := range fields {
		err := my.AddFormField(name, value)
		if err != nil {
			return err
		}
	}

	return nil
}

// ContentType 获取 content type。
// 示例: multipart/form-data; boundary=a721db848d426955c840df516f4b55f00a55a2b8b615f58e903854a52da8
func (my *MultipartWrapper) ContentType() string {
	return my.bodyWriter.FormDataContentType()
}

// Close 关闭 MultipartWriter。
func (my *MultipartWrapper) Close() {
	my.bodyWriter.Close()
}

// BodyReader 获取 reader 用于读取 multipart 的数据。
func (my *MultipartWrapper) BodyReader() io.Reader {
	return my.bodyBuffer
}
