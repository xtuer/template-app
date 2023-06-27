package bean

// FileTransferForm 文件传输的数据对象。
type FileTransferForm struct {
	SrcPath    string `json:"srcPath"`    // 源文件或者目录路径
	DstDir     string `json:"dstDir"`     // 保存上传文件的目录
	TargetIp   string `json:"targetIp"`   // 上传文件的目标机器 IP
	TargetPort int    `json:"targetPort"` // 上传文件的目标机器 Port
}

// NewFileTransferForm 创建 FileTransferForm 对象。
func NewFileTransferForm() *FileTransferForm {
	return &FileTransferForm{}
}
