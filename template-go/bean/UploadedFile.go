package bean

// UploadedFile 为上传的文件结构体。
type UploadedFile struct {
	Uid       string               `json:"uid"`              // 唯一 ID: <fileMd5>-<dstDirMd5>
	FileName  string               `json:"fileName"`         // 文件名
	FileSize  int64                `json:"fileSize"`         // 文件大小 (Bytes)
	FileMd5   string               `json:"fileMd5"`          // 文件的 MD5
	DstDir    string               `json:"dstDir"`           // 文件保存目录
	State     int                  `json:"state"`            // 上传状态: 0 (初始化)、1 (合并中)、2 (合并失败)、3 (合并成功)
	ChunkSize int64                `json:"chunkSize"`        // 文件上传的分片大小
	Chunks    []*UploadedFileChunk `json:"chunks,omitempty"` // 文件上传的分片信息
}

// DstPath 构建文件保存路径。
func (o UploadedFile) DstPath() string {
	return o.DstDir + "/" + o.FileName
}

// UploadedFileChunk 为上传的分片信息。
type UploadedFileChunk struct {
	Sn       int    `json:"sn"`       // 分片的序号，按序号合并分片
	Md5      string `json:"md5"`      // 分片的 MD5
	StartPos int64  `json:"startPos"` // 分片在文件中的起始位置
	EndPos   int64  `json:"endPos"`   // 分片装文件中的结束位置 (不包含 EndPos)
	State    int    `json:"state"`    // 分片上传状态: 0 (初始化)、1 (上传中)、2 (上传失败)、3 (上传成功)
}

// NewUploadedFile 创建上传的文件。
func NewUploadedFile() *UploadedFile {
	return &UploadedFile{Chunks: make([]*UploadedFileChunk, 0)}
}

// NewUploadedFileChunk 创建上传的文件分片。
func NewUploadedFileChunk() *UploadedFileChunk {
	return &UploadedFileChunk{}
}
