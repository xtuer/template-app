package repo

import (
	"xtuer/bean"
)

// UploadRepo 为上传数据管理的类。
type UploadRepo struct {
	ufiles map[string]*bean.UploadedFile
}

// NewUploadRepo 创建 NewUploadRepo 对象。
func NewUploadRepo() *UploadRepo {
	return &UploadRepo{
		ufiles: make(map[string]*bean.UploadedFile),
	}
}

// FindUploadedFile 根据文件 Uid 获取上传信息。
func (o *UploadRepo) FindUploadedFile(fileUid string) *bean.UploadedFile {
	uf, ok := o.ufiles[fileUid]

	if ok {
		return uf
	} else {
		return nil
	}
}

// CreateUploadedFile 创建上传信息。
func (o *UploadRepo) CreateUploadedFile(fileUid string, uf *bean.UploadedFile) {
	o.ufiles[fileUid] = uf
}

// DeleteUploadedFile 删除文件上传信息。
func (o *UploadRepo) DeleteUploadedFile(fileUid string) {
	delete(o.ufiles, fileUid)
}

// UpdateUploadedFileState 更新上传文件的状态。
func (o *UploadRepo) UpdateUploadedFileState(fileUid string, state int) {
	if uf := o.FindUploadedFile(fileUid); uf != nil {
		uf.State = state
	}
}

// FindChunk 获取上传文件的第 chunkSn 个分片。
// 返回分片对象，如查询不到则返回 nil。
func (o *UploadRepo) FindChunk(fileUid string, chunkSn int) *bean.UploadedFileChunk {
	uf := o.FindUploadedFile(fileUid)
	if uf == nil || uf.Chunks == nil {
		return nil
	}

	for _, chunk := range uf.Chunks {
		if chunk.Sn == chunkSn {
			return chunk
		}
	}

	return nil
}

// UpdateChunkMd5 更新分片的 MD5。
func (o *UploadRepo) UpdateChunkMd5(fileUid string, chunkSn int, chunkMd5 string) {
	chunk := o.FindChunk(fileUid, chunkSn)
	if chunk == nil {
		return
	}

	chunk.Md5 = chunkMd5
	o.UpdateChunk(fileUid, chunk)
}

// UpdateChunkState 更新分片的状态。
func (o *UploadRepo) UpdateChunkState(fileUid string, chunkSn int, state int) {
	chunk := o.FindChunk(fileUid, chunkSn)
	if chunk == nil {
		return
	}

	chunk.State = state
	o.UpdateChunk(fileUid, chunk)
}

// UpdateChunk 更新上传文件的分片。
func (o *UploadRepo) UpdateChunk(fileUid string, chunk *bean.UploadedFileChunk) {
	uf := o.FindUploadedFile(fileUid)
	if uf == nil || uf.Chunks == nil {
		return
	}

	for i, ch := range uf.Chunks {
		if chunk.Sn == ch.Sn {
			uf.Chunks[i] = chunk
			return
		}
	}
}

// CountUploadedFile 获取上传的文件数量。
func (o *UploadRepo) CountUploadedFile() int {
	return len(o.ufiles)
}

// CountUploadingFile 获取上传中的文件数量。
func (o *UploadRepo) CountUploadingFile() int {
	var count int

	// 1 (合并成功)、2 (合并失败)
	for _, uf := range o.ufiles {
		if uf.State != bean.US_Success && uf.State != bean.US_Failed {
			count++
		}
	}

	return count
}
