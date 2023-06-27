package controller

import (
	"net/http"
	"xtuer/bean"
	"xtuer/service"

	"github.com/gin-gonic/gin"
)

// UploadController 为上传文件控制器。
type UploadController struct {
	uploadService *service.UploadService // 上传服务。
}

// NewUploadController 创建上传文件控制器对象。
func NewUploadController(uploadService *service.UploadService) *UploadController {
	return &UploadController{uploadService}
}

// RegisterRoutes 注册当前控制器的路由。
func (o *UploadController) RegisterRoutes(router *gin.Engine) {
	router.GET(API_UPLOADS_BY_FILE_UID, R(o.FindUploadedFile()))
	router.POST(API_UPLOADS, R(o.CreateUploadedFile()))
	router.POST(API_UPLOADS_CHUNK, R(o.UploadFileChunk()))
}

// FindUploadedFile 根据文件 MD5 获取上传信息。
// 链接: http://localhost:8080/api/uploads/:fileUid
// 参数: 无
// 方法: GET
// 响应: payload 为 UploadedFile 对象。
// 测试: curl http://localhost:8080/api/uploads/abc
func (o *UploadController) FindUploadedFile() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		fileUid := c.Param("fileUid")
		uf := o.uploadService.FindUploadedFile(fileUid)

		if uf != nil {
			return OkResponseWithData(uf)
		} else {
			return ErrorResponseWithMessage("没有文件上传信息")
		}
	}
}

// CreateUploadedFile 创建文件上传的信息。
// 链接: http://localhost:8080/api/uploads
// 参数: 无
// 方法: POST
// 请求体: {"fileName": "xx.zip", "fileMd5": "abcd", "fileSize": 922222220, "dstDir": "/root/temp"}
// 响应: payload 为 UploadedFile 对象，包含计算出的分片信息。
// 测试: curl -X POST http://localhost:8080/api/uploads -d '{"fileName": "xx.zip", "fileMd5": "abcd", "fileSize": 922222220, "dstDir": "/root/temp"}' -H 'Content-Type: application/json'
func (o *UploadController) CreateUploadedFile() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		uf := bean.NewUploadedFile()

		// 参数获取: 把请求体中的数据绑定到 uf
		// 参数有 fileName, fileSize, fileMd5, dstDir
		if err := c.ShouldBindJSON(uf); err != nil {
			return ErrorResponseWithMessage(err, http.StatusBadRequest)
		}

		uf, err := o.uploadService.CreateUploadedFile(uf.FileName, uf.FileMd5, uf.FileSize, uf.DstDir)

		if err != nil {
			return ErrorResponseWithMessage(err)
		} else {
			return OkResponseWithData(uf)
		}
	}
}

// UploadFileChunk 上传文件的分片
// 链接: http://localhost:8080/api/uploads/chunks
// 参数: md5, sn and file
// 方法: POST
// 响应: payload 为分片对象 UploadedFileChunk。
// 测试: curl -X POST http://localhost:8080/api/uploads/:fileUid/chunks -F "file=@/Users/biao/Documents/temp/x.sh" -F "md5=123" -F "sn=1" -H "Content-Type: multipart/form-data"
func (o *UploadController) UploadFileChunk() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		chunk, err := o.uploadService.UploadFileChunk(c)

		if err != nil {
			return ErrorResponseWithMessage(err)
		} else {
			return OkResponseWithData(chunk)
		}
	}
}
