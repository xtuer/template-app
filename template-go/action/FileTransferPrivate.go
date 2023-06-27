package action

import (
	"fmt"
	"net/http"
	"newdtagent/bean"
	"newdtagent/log"
	"newdtagent/security"
	"newdtagent/utils"
	"os"
	"sync"
	"time"

	"github.com/sirupsen/logrus"
)

const (
	URL_UPLOADED_FILE_CREATE = "http://${ip}:${port}/api/uploads"
	URL_UPLOADED_FILE_STATUS = "http://${ip}:${port}/api/uploads/${file_uid}"
	URL_CHUNK_UPLOAD         = "http://${ip}:${port}/api/uploads/${file_uid}/chunks"
)

const (
	MAX_RETRY_COUNT       = 20 // 出错的最大尝试次数 (4.3G 的文件在有的环境合并使用了 58S)。
	MAX_UPLOAD_CONCURRENT = 5  // 上传最大并发数量
)

// CreateUploadedFile 创建上传文件。
func (my *FileTransfer) createUploadedFile() error {
	/**
	创建上传文件逻辑:
	1. 获取文件信息。
	2. 创建上传文件需要的信息。
	3. 准备请求认证的 header 和 url。
	4. 执行创建上传文件的请求，并把结果反序列化为 Response 对象。
	*/

	// [1] 获取文件信息。
	file, err := os.Open(my.SrcPath)
	if err != nil {
		return err
	}
	defer file.Close()

	fileInfo, err := file.Stat()
	if err != nil {
		return err
	}

	// [2] 创建上传文件需要的信息。
	data := map[string]interface{}{
		"fileName": fileInfo.Name(),
		"fileMd5":  utils.Md5OfFile(my.SrcPath),
		"fileSize": fileInfo.Size(),
		"dstDir":   my.DstDir,
	}

	// [3] 准备请求认证的 header 和 url。
	url := bean.String(URL_UPLOADED_FILE_CREATE).ReplacePlaceholders(map[string]interface{}{
		"ip":   my.TargetIp,
		"port": my.TargetPort,
	})

	// [4] 执行创建上传文件的请求，并把结果反序列化为 Response 对象。
	rsp := bean.Response{
		Data: &bean.UploadedFile{},
	}
	err = utils.PostJson(url, &rsp, data, security.SignHeaders())
	if err != nil {
		return err
	}

	if !rsp.Success {
		return fmt.Errorf("创建上传文件失败")
	}

	my.uf = rsp.Data.(*bean.UploadedFile)

	return nil
}

// GetUploadedFile 获取上传文件。
func (my *FileTransfer) getUploadedFile() error {
	/**
	获取逻辑:
	1. 准备请求认证的 header 和 url。
	2. 执行获取上传文件的请求，并把结果反序列化为 Response 对象。
	*/

	// [1] 准备请求认证的 header 和 url。
	url := bean.String(URL_UPLOADED_FILE_STATUS).ReplacePlaceholders(map[string]interface{}{
		"ip":       my.TargetIp,
		"port":     my.TargetPort,
		"file_uid": my.uf.Uid,
	})

	// [2] 执行获取上传文件的请求，并把结果反序列化为 Response 对象。
	rsp := bean.Response{
		Data: &bean.UploadedFile{},
	}
	err := utils.GetJson(url, &rsp, security.SignHeaders())
	if err != nil {
		return err
	}

	if !rsp.Success {
		return fmt.Errorf("获取上传文件失败, fileUid: %s", my.uf.Uid)
	}

	my.uf = rsp.Data.(*bean.UploadedFile)

	return nil
}

// TransferFile 复制文件到目标 agent。
func (my *FileTransfer) doTransferFile() error {
	/**
	逻辑:
	1. 请求文件上传信息。
	2. 根据 uf 的状态分类处理:
	   2.1 上传成功
	   2.2 分片合并中，稍后继续请求状态
	   2.3 合并分片失败，例如 MD5 不匹配，创建保存目录失败
	   2.4 初始化，上传分片
	3. 获取最新上传文件状态，重复步骤 1 直到文件上传成功或者合并分片出错。
	*/

	// [1] 请求文件上传信息。
	log.Log.WithFields(logrus.Fields{
		"ip":      my.TargetIp,
		"port":    my.TargetPort,
		"srcPath": my.SrcPath,
		"dstDir":  my.DstDir,
		"uid":     my.Uid,
	}).Info("上传文件到其他 Agent")

	err := my.createUploadedFile()
	if err != nil {
		return err
	}

	count := 0

	for count < MAX_RETRY_COUNT {
		count++
		state := my.uf.State

		if state == bean.US_Success {
			// [2.1] 上传成功
			log.Log.WithFields(logrus.Fields{
				"ip":      my.TargetIp,
				"port":    my.TargetPort,
				"srcPath": my.SrcPath,
				"dstDir":  my.DstDir,
				"size":    my.uf.FileSize,
				"uid":     my.Uid,
			}).Info("[成功] 上传文件成功")

			return nil
		} else if state == bean.US_Handling {
			// [2.2] 分片合并中，稍后继续请求状态
			log.Log.WithFields(logrus.Fields{
				"ip":      my.TargetIp,
				"port":    my.TargetPort,
				"srcPath": my.SrcPath,
				"dstDir":  my.DstDir,
				"uid":     my.Uid,
			}).Info("文件正在合并中...")
		} else if state == bean.US_Failed {
			// [2.3] 合并分片失败，例如 MD5 不匹配，创建保存目录失败
			log.Log.WithFields(logrus.Fields{
				"ip":      my.TargetIp,
				"port":    my.TargetPort,
				"srcPath": my.SrcPath,
				"dstDir":  my.DstDir,
				"size":    my.uf.FileSize,
				"uid":     my.Uid,
			}).Warn("[错误] 上传文件失败")

			return fmt.Errorf("上传文件失败, transferUid [%s], srcPath [%s], fileUid [%s]", my.Uid, my.SrcPath, my.uf.Uid)
		} else {
			// [2.4] 初始化，上传分片
			my.uploadFileChunks()
		}

		// [3] 获取最新上传文件状态，重复步骤 1 直到文件上传成功或者合并分片出错。
		time.Sleep(5 * time.Second) // 分片合并也需要一些时间，所以等一下。
		err = my.getUploadedFile()
		if err != nil {
			return err
		}
	}

	return fmt.Errorf("传输文件超过最多等待次数")
}

// uploadFileChunks 上传需要上传的分片。
func (my *FileTransfer) uploadFileChunks() (resultErr error) {
	/**
	上传分片逻辑:
	1. 获取需要上传的分片。
	2. 并发上传分片: 控制并发量，等待所有分片上传完成。
	*/

	// 需要上传的分片。
	needUploadedChunks := []*bean.UploadedFileChunk{}

	// [1] 获取需要上传的分片。
	for _, chunk := range my.uf.Chunks {
		// 上传未上传、或者重传失败的分片 (分片上传失败例如不能创建分片的目录)。
		if chunk.State == bean.US_Init || chunk.State == bean.US_Failed {
			needUploadedChunks = append(needUploadedChunks, chunk)
		}
	}

	// [2] 并发上传分片: 控制并发量，等待所有分片上传完成。
	ch := make(chan struct{}, MAX_UPLOAD_CONCURRENT)
	wg := sync.WaitGroup{}
	for _, chunk := range needUploadedChunks {
		ch <- struct{}{}
		wg.Add(1)

		go func(chunk *bean.UploadedFileChunk) {
			defer func() {
				<-ch
				wg.Done()
			}()

			log.Log.WithFields(logrus.Fields{
				"ip":      my.TargetIp,
				"port":    my.TargetPort,
				"srcPath": my.SrcPath,
				"sn":      chunk.Sn,
				"size":    (chunk.EndPos - chunk.StartPos),
				"uid":     my.Uid,
			}).Debug("上传分片")

			rspChunk, err := my.uploadFileChunk(chunk.Sn, chunk.StartPos, chunk.EndPos)

			if err == nil {
				chunk.State = rspChunk.State
				chunk.Md5 = rspChunk.Md5
			} else {
				resultErr = err

				log.Log.WithFields(logrus.Fields{
					"ip":      my.TargetIp,
					"port":    my.TargetPort,
					"srcPath": my.SrcPath,
					"sn":      chunk.Sn,
					"error":   err,
					"uid":     my.Uid,
				}).Warn("[错误] 上传分片失败")
			}
		}(chunk)
	}
	wg.Wait()

	return resultErr
}

// uploadFileChunk 上传的分片。
func (my *FileTransfer) uploadFileChunk(chunkSn int, chunkStartPos, chunkEndPos int64) (chunk bean.UploadedFileChunk, err error) {
	/**
	上传分片逻辑:
	1. 读取分片的内容创建 multipart。
	2. 创建 URL。
	3. 创建请求。
	4. 设置 Headers。
	5. 发送请求。
	*/

	// [1] 读取分片的内容创建 multipart。
	mw := bean.NewMultipartWrapper()
	chunkSize := chunkEndPos - chunkStartPos
	chunkMd5, err := mw.AddFileChunk(my.SrcPath, chunkStartPos, uint(chunkSize))
	if err != nil {
		return chunk, err
	}

	err = mw.AddFormFields(map[string]interface{}{
		"sn":  chunkSn,
		"md5": chunkMd5,
	})
	if err != nil {
		return chunk, err
	}

	mw.Close()

	// [2] 创建 URL。
	url := bean.String(URL_CHUNK_UPLOAD).ReplacePlaceholders(map[string]interface{}{
		"ip":       my.TargetIp,
		"port":     my.TargetPort,
		"file_uid": my.uf.Uid,
	})

	// [3] 创建请求。
	req, err := http.NewRequest("POST", url, mw.BodyReader())
	if err != nil {
		return chunk, err
	}

	// [4] 设置 Headers。
	for name, value := range security.SignHeaders() {
		req.Header.Set(name, value.(string))
	}
	req.Header.Set("Content-Type", mw.ContentType())

	// [5] 发送请求。
	client := &http.Client{}
	rsp, err := client.Do(req)
	if err != nil {
		return chunk, err
	}
	defer rsp.Body.Close()

	// [6] 反序列化获取响应的分片对象。
	uploadRsp := &bean.Response{
		Data: &bean.UploadedFileChunk{}, // 需要指针类型才能正确的反序列化
	}
	err = utils.ResponseToBean(rsp, uploadRsp)
	if err != nil {
		return chunk, err
	}
	if !uploadRsp.Success {
		return chunk, fmt.Errorf(uploadRsp.Msg)
	}

	chunk = *uploadRsp.Data.(*bean.UploadedFileChunk)
	return chunk, nil
}
