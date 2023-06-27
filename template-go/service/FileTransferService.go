package service

import (
	"fmt"
	"newdtagent/action"
)

// FileTransferService 文件传输服务。
type FileTransferService struct {
	transfers map[string]*action.FileTransfer
}

// NewFileTransferService 创建文件传输服务对象。
func NewFileTransferService() *FileTransferService {
	return &FileTransferService{
		transfers: make(map[string]*action.FileTransfer),
	}
}

// TransferFile 传输当前系统中的文件 srcPath 到目标机器 targetIp 上的 dstDir 中。
func (my *FileTransferService) TransferFile(targetIp string, targetPort int, srcPath, dstDir string) (transfer *action.FileTransfer, err error) {
	/**
	传输逻辑:
	1. 参数校验。
	2. 异步执行文件传输。
	*/

	// [1] 参数校验。
	if targetIp == "" {
		return nil, fmt.Errorf("targetIp 不能为空: %s", targetIp)
	}
	if targetPort <= 0 {
		return nil, fmt.Errorf("端口无效: %d", targetPort)
	}
	if srcPath == "" {
		return nil, fmt.Errorf("srcPath 不能为空: %s", srcPath)
	}
	if dstDir == "" {
		return nil, fmt.Errorf("dstDir 不能为空: %s", srcPath)
	}

	// [2] 异步执行文件传输。
	transfer = action.NewFileTransfer(targetIp, targetPort, srcPath, dstDir)
	my.transfers[transfer.Uid] = transfer

	go transfer.TransferFile()

	return transfer, nil
}

// TransferDir 传输当前系统中的目录 srcDir 到目标机器 targetIp 上的 dstDir 中，并且保存目录结构不变。
func (my *FileTransferService) TransferDir(targetIp string, targetPort int, srcDir, dstDir string) error {
	return nil
}

// FindFileTransferByUid 查询传入 transferUid 对应的传输对象。
func (my *FileTransferService) FindFileTransferByUid(transferUid string) *action.FileTransfer {
	if transfer, ok := my.transfers[transferUid]; ok {
		return transfer
	} else {
		return nil
	}
}
