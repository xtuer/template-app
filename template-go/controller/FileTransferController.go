package controller

import (
	"net/http"
	"newdtagent/bean"
	"newdtagent/service"

	"github.com/gin-gonic/gin"
)

// FileTransferController 文件传输控制器。
type FileTransferController struct {
	transferService *service.FileTransferService // 文件传输服务
}

// NewFileTransferController 创建文件传输控制器对象。
func NewFileTransferController(transferService *service.FileTransferService) *FileTransferController {
	return &FileTransferController{
		transferService: transferService,
	}
}

// [3] 注册当前控制器的路由。
func (my *FileTransferController) RegisterRoutes(router *gin.Engine) {
	router.POST(API_TRANSFERS_FILE, R(my.TransferFile()))
	router.GET(API_TRANSFERS_BY_UID, R(my.FindFileTransferByUid()))
}

// TransferFile 传输文件。
// 链接: http://localhost:8080/api/transfers/file
// 参数: targetIp, targetPort, srcPath, dstPath
// 方法: POST
// 响应: payload 为无。
// 测试: curl -X POST http://localhost:8080/api/transfers/file -d '{"targetIp": "127.0.0.1", "targetPort": 8080, "srcPath": "/data/todo.txt", "dstDir": "/root/temp"}' -H 'Content-Type: application/json'
func (my *FileTransferController) TransferFile() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		// 参数获取: 把请求体中的数据绑定到 form
		// 参数有 targetIp, targetPort, srcPath, dstPath
		form := bean.NewFileTransferForm()
		if err := c.ShouldBindJSON(form); err != nil {
			return ErrorResponseWithMessage(err, http.StatusBadRequest)
		}

		transfer, err := my.transferService.TransferFile(form.TargetIp, form.TargetPort, form.SrcPath, form.DstDir)
		if err != nil {
			return ErrorResponseWithMessage(err)
		} else if transfer.Error != "" {
			return ErrorResponseWithMessageAndData(transfer.Error, transfer)
		} else {
			return OkResponseWithData(transfer)
		}
	}
}

// FindFileTransferByUid 查询传入 transferUid 对应的传输对象。
// 链接: http://localhost:8080/api/transfers/:transferUid
// 参数: 无
// 方法: GET
// 响应: payload 为 FileTransfer 对象。
// 测试: curl http://localhost:8080/api/transfers/xxxx
func (my *FileTransferController) FindFileTransferByUid() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		// 获取路径上的参数 transferUid
		transferUid := c.Param("transferUid")
		transfer := my.transferService.FindFileTransferByUid(transferUid)

		if transfer != nil {
			return OkResponseWithData(transfer)
		} else {
			return ErrorResponseWithMessage("文件传输任务不存在")
		}
	}
}
