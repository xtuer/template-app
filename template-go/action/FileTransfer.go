package action

import (
	"newdtagent/bean"
	"newdtagent/log"
	"newdtagent/utils"
	"strings"

	"github.com/sirupsen/logrus"
)

// FileTransfer 文件传输服务 (Agent 之间传输文件)。
type FileTransfer struct {
	Uid        string `json:"uid"`        // 唯一 ID
	State      int    `json:"state"`      // 传输状态
	Error      string `json:"error"`      // 传输错误
	TargetIp   string `json:"targetIp"`   // 目标机器 Ip
	TargetPort int    `json:"targetPort"` // 目标机器端口
	SrcPath    string `json:"srcPath"`    // 要上传文件的路径
	DstDir     string `json:"dstDir"`     // 目标机器上保存上传文件的目录

	uf *bean.UploadedFile // 上传的文件信息 (服务器端返回)
}

// NewFileTransfer 创建 FileTransfer 对象。
func NewFileTransfer(targetIp string, targetPort int, srcPath, dstDir string) *FileTransfer {
	// 去掉路径最后面的 /
	srcPath = strings.TrimRight(srcPath, "/")
	dstDir = strings.TrimRight(dstDir, "/")
	targetIp = strings.TrimSpace(targetIp)

	return &FileTransfer{
		Uid:        "transfer-" + utils.Uid(),
		State:      bean.US_Init,
		TargetIp:   targetIp,
		TargetPort: targetPort,
		SrcPath:    srcPath,
		DstDir:     dstDir,
	}
}

// TransferFile 复制文件到目标 agent。
func (my *FileTransfer) TransferFile() {
	my.State = bean.US_Handling
	err := my.doTransferFile()

	if err == nil {
		my.State = bean.US_Success
	} else {
		my.State = bean.US_Failed
		my.Error = err.Error()

		log.Log.WithFields(logrus.Fields{
			"ip":      my.TargetIp,
			"port":    my.TargetPort,
			"srcPath": my.SrcPath,
			"dstDir":  my.DstDir,
			"uid":     my.Uid,
			"error":   my.Error,
		}).Info("[错误] 上传文件到其他 Agent")
	}
}
