package service

import (
	"errors"
	"fmt"
	"os"
	"strconv"
	"strings"
	"sync"
	"xtuer/bean"
	"xtuer/config"
	"xtuer/log"
	"xtuer/repo"
	"xtuer/utils"

	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

// UploadService 为上传文件服务。
type UploadService struct {
	mergeLock    *sync.Mutex      // 合并分片使用的锁。
	chunkBaseDir string           // 文件分片存储的临时目录。
	uploadRepo   *repo.UploadRepo // 上传文件的 Repo。
}

// NewUploadService 创建上传服务对象。
func NewUploadService(uploadRepo *repo.UploadRepo) *UploadService {
	return &UploadService{
		mergeLock:    &sync.Mutex{},
		chunkBaseDir: config.GetAppConfig().UploadTempDir,
		uploadRepo:   uploadRepo,
	}
}

// FindUploadedFile 根据文件 Uid 获取上传信息。
func (o *UploadService) FindUploadedFile(fileUid string) *bean.UploadedFile {
	return o.uploadRepo.FindUploadedFile(fileUid)
}

// CreateUploadedFile 创建文件上传信息。
func (o *UploadService) CreateUploadedFile(fileName, fileMd5 string, fileSize int64, dstDir string) (uf *bean.UploadedFile, err error) {
	/**
	 逻辑:
	 1. 如果文件上传信息已经存在:
	    1.1 上传完成，但目标文件不存在，删除已有上传信息重新创建。
		1.2 其他情况返回已存在的上传文件信息，不重复创建。
	 2. 创建文件上传对象。
	 3. 数据校验。
	 4. 如果目标文件已经存在，则不需要重复上传。
	 5. 创建分片信息。
	 6. 把文件上传信息保存起来。
	*/

	// [1] 如果文件上传信息已经存在:
	fileUid := o.generateUploadedFileUid(fileMd5, strings.TrimSpace(dstDir))
	uf = o.FindUploadedFile(fileUid)

	log.Log.WithFields(logrus.Fields{
		"fileMd5":  fileMd5,
		"fileName": fileName,
		"dstPath":  dstDir + "/" + fileName,
	}).Info("上传文件")

	if uf != nil {
		if uf.State == bean.US_Success && !utils.FileExist(uf.DstPath()) {
			// [1.1] 上传完成，但目标文件不存在，删除已有上传信息重新创建。
			o.uploadRepo.DeleteUploadedFile(fileUid)

			log.Log.WithFields(logrus.Fields{
				"fileMd5":  fileMd5,
				"fileName": fileName,
				"dstPath":  uf.DstPath(),
			}).Warn("上传信息已经存在，曾经上传成功，但目标文件已经不存在，删除已有上传信息重新创建")
		} else {
			// [1.2] 其他情况返回已存在的上传文件信息，不重复创建。
			log.Log.WithFields(logrus.Fields{
				"fileMd5":  fileMd5,
				"fileName": fileName,
				"state":    uf.State,
			}).Info("上传信息已经存在，不再重复创建，返回当前的上传信息")

			return uf, nil
		}
	}

	// [2] 创建文件上传对象。
	uf = bean.NewUploadedFile()
	uf.Uid = fileUid
	uf.FileName = strings.TrimSpace(fileName)
	uf.FileMd5 = fileMd5
	uf.FileSize = fileSize
	uf.DstDir = strings.TrimSpace(dstDir)
	uf.ChunkSize = bean.Upload_File_Chunk_Size // 文件分片大小: 5M

	// [3] 数据校验。
	if err := o.validateUploadedFile(uf); err != nil {
		return nil, err
	}

	// [4] 如果目标文件已经存在，则不需要重复上传。
	dstPath := uf.DstPath()
	if utils.FileExist(dstPath) && uf.FileMd5 == utils.Md5OfFile(dstPath) {
		// 提示: 目标文件存在，但是数据库里没有记录，所以不需要操作数据库。
		uf.State = bean.US_Success

		log.Log.WithFields(logrus.Fields{
			"dstPath": dstPath,
		}).Info("目标文件已经存在，不需要重复上传，上传完成")

		return uf, nil
	}

	// [5] 创建分片信息。
	last := false

	for sn := 0; ; sn = sn + 1 {
		startPos := int64(sn) * uf.ChunkSize
		endPos := startPos + uf.ChunkSize

		// 达到文件大小的时候，说明是最后一个分片。
		if endPos >= uf.FileSize {
			endPos = uf.FileSize
			last = true
		}

		// 分片对象。
		ufc := &bean.UploadedFileChunk{
			Sn:       sn,
			StartPos: startPos,
			EndPos:   endPos,
			Md5:      "", // 分片的 MD5 由前端上传文件时一起传过来。
			State:    bean.US_Init,
		}
		uf.Chunks = append(uf.Chunks, ufc)

		// 创建完最后一个分片时结束循环。
		if last {
			break
		}
	}

	log.Log.WithFields(logrus.Fields{
		"fileName":   uf.FileName,
		"fileMd5":    uf.FileMd5,
		"fileSize":   uf.FileSize,
		"dstDir":     uf.DstDir,
		"chunkSize":  uf.ChunkSize,
		"chunkCount": len(uf.Chunks),
	}).Info("创建文件上传信息")

	// [6] 把文件上传信息保存起来。
	o.uploadRepo.CreateUploadedFile(uf.Uid, uf)

	return uf, nil
}

// validateUploadedFile 校验上传文件的信息。
func (o *UploadService) validateUploadedFile(uf *bean.UploadedFile) error {
	var errMsg string

	if uf.FileName == "" {
		errMsg = "文件名 fileName 不能为空"
	}
	if uf.FileMd5 == "" {
		errMsg = "文件的 fileMd5 不能为空"
	}
	if uf.FileSize <= 0 {
		errMsg = "文件大小 fileSize 不能为负数或者 0"
	}
	if uf.DstDir == "" {
		errMsg = "文件的保存目录 dstDir 不能为空"
	}

	if errMsg != "" {
		return errors.New(errMsg)
	} else {
		return nil
	}
}

// UploadFileChunk 上传文件分片。
func (o *UploadService) UploadFileChunk(c *gin.Context) (chunk *bean.UploadedFileChunk, err error) {
	/**
	 逻辑:
	 1. 获取文件的 Uid，分片的 MD5 和 sn。
	 2. 查询分片信息:
	    2.1 如果分片不存在则返回。
		2.2 上传中、上传成功直接返回。
		2.3 初始化、上传失败则继续上传。
	 3. 保存分片到临时目录，分片保存路径为 <working-directory>/upload-tmp/<fileMd5>-<dstDirMd5>/<sn>.tmp。
	 4. 验证分片的 MD5。
	 5. 如果所有分片都上传完成，则合并成完整文件，并且删除分片。
	*/

	// [1] 获取分片的 MD5 和 sn。
	fileUid := c.Param("fileUid")
	chunkMd5 := c.PostForm("md5")
	chunkSn, err := strconv.Atoi(c.PostForm("sn"))
	if err != nil {
		return nil, errors.New("chunk sn 必须为整数")
	}

	// [2.1] 如果分片不存在则返回。
	chunk = o.uploadRepo.FindChunk(fileUid, chunkSn)
	if chunk == nil {
		return nil, fmt.Errorf("上传的分片不存在, sn: %d", chunkSn)
	}

	// [2.2] 上传中、上传成功直接返回。
	if chunk.State == bean.US_Handling || chunk.State == bean.US_Success {
		log.Log.WithFields(logrus.Fields{
			"md5":   chunkMd5,
			"sn":    chunk.Sn,
			"state": chunk.State,
		}).Info("分片正在上传中或者已上传成功，不需要重复上传")
		return chunk, nil
	}

	// [2.3] 初始化、上传失败则继续上传。
	// [3] 保存分片到临时目录，分片保存路径为 <working-directory>/upload-tmp/<md5>-<dstDirMd5>/<sn>.tmp。
	chunkFile, _ := c.FormFile("file")
	chunkPath := o.generateChunkPath(fileUid, chunk.Sn)

	log.Log.WithFields(logrus.Fields{
		"md5":  chunkMd5,
		"sn":   chunk.Sn,
		"size": chunkFile.Size,
		"dst":  chunkPath,
	}).Info("上传分片")

	// [*] 目录不存在会报错，所以需要事先创建，保存时目标文件会自动覆盖。
	// 修改分片状态为上传中。
	o.updateChunkState(fileUid, chunk, bean.US_Handling)
	os.MkdirAll(o.generateChunkDir(fileUid), os.ModePerm)
	err = c.SaveUploadedFile(chunkFile, chunkPath)

	if err != nil {
		o.updateChunkState(fileUid, chunk, bean.US_Failed)
		return nil, err
	}

	// [4] 验证分片的 MD5。
	chunk.Md5 = chunkMd5
	o.uploadRepo.UpdateChunkMd5(fileUid, chunkSn, chunkMd5)
	tempMd5 := utils.Md5OfFile(chunkPath)
	if tempMd5 != chunkMd5 {
		log.Log.WithFields(logrus.Fields{
			"fileMd5": fileUid,
			"passMd5": chunkMd5,
			"currMd5": tempMd5,
		}).Warn("上传分片错误，MD5 不匹配")

		// 上传失败。
		o.updateChunkState(fileUid, chunk, bean.US_Failed)
		return nil, fmt.Errorf("分片的 MD5 不匹配，文件的 MD5: %s, 传入的 MD5: %s, 保存后计算得到的 MD5: %s", fileUid, chunkMd5, tempMd5)
	}

	// 上传成功。
	o.updateChunkState(fileUid, chunk, bean.US_Success)

	// [5] 如果所有分片都上传完成，则异步合并成完整文件，并且删除分片文件。
	go o.mergeUploadedFileWhenAllChunksSuccessfullyUploaded(fileUid)

	return chunk, nil
}

// generateChunkDir 构建文件分片保存的临时目录路径。
// chunk 目录路径格式: <chunkBaseDir>/<fileMd5>-<dstDirMd5>
func (o *UploadService) generateChunkDir(fileUid string) string {
	return fmt.Sprintf("%s/%s", o.chunkBaseDir, fileUid)
}

// generateChunkPath 构建文件分片的保存路径。
// chunk 路径格式: <chunkBaseDir>/<fileMd5>-<dstDirMd5>/<chunkSn>.tmp
func (o *UploadService) generateChunkPath(fileUid string, chunkSn int) string {
	return fmt.Sprintf("%s/%s/%d.tmp", o.chunkBaseDir, fileUid, chunkSn)
}

// generateUploadedFileUid 生成上传文件的唯一 ID。
// 上传文件的 Uid 格式: <fileMd5>-<dstDirMd5>
func (o *UploadService) generateUploadedFileUid(fileMd5, dstDir string) string {
	return fmt.Sprintf("%s-%s", fileMd5, utils.Md5(dstDir))
}

// mergeUploadedFileWhenAllChunksSuccessfullyUploaded 当所有分片都上传成功后把所有分片按序号合并成文件。
func (o *UploadService) mergeUploadedFileWhenAllChunksSuccessfullyUploaded(fileUid string) {
	/**
	 逻辑:
	 1. 如果不需要合并则返回。
	 2. 如果需要合并则加锁，并且获得锁后再次判断是否需要合并
	    * 进行 2 次是否需要合并文件的判断是因为文件合并是一个耗时操作，高并发测试时多个访问可能同时得到同一个文件需要合并，但是一个文件只能合并一次。
		* 这里是有优化空间的，每个文件合并使用独立的锁，而不是一个全局的锁。
	 3. 如果目标文件已存在:
	    3.1 目标文件的 MD5 和上传文件的 MD5 相同，则更新文件上传状态为完成，并删除上传的分片。
		3.2 目标文件的 MD5 和上传文件的 MD5 不相同，则删除已存在的目标文件。
	 4. 如果目标目录不存在则创建。
	 5. 按照分片顺序合并分片到目标文件，合并成功后删除分片文件。
	*/

	// [1] 如果不需要合并则返回。
	if !o.needMerge(fileUid) {
		return
	}

	// [2] 如果需要合并则加锁，并且获得锁后再次判断是否需要合并。
	o.mergeLock.Lock()
	defer o.mergeLock.Unlock()

	if !o.needMerge(fileUid) {
		return
	}

	uf := o.uploadRepo.FindUploadedFile(fileUid)
	o.updateUploadedFileState(uf, bean.US_Handling)

	// [3] 如果目标文件存在:
	dstPath := uf.DstPath()
	if utils.FileExist(dstPath) {
		tempMd5 := utils.Md5OfFile(dstPath)

		if tempMd5 == uf.FileMd5 {
			log.Log.WithFields(logrus.Fields{
				"dstPath": dstPath,
			}).Info("目标文件已经存在，不需要重复合并")

			// [3.1] 目标文件的 MD5 和上传文件的 MD5 相同，则更新文件上传状态为完成，并删除上传的分片。
			o.updateUploadedFileState(uf, bean.US_Success)

			// 删除分片文件目录。
			o.removeChunkDir(uf.Uid)
			return
		} else {
			// [3.2] 目标文件的 MD5 和上传文件的 MD5 不相同，则删除已存在的目标文件。
			log.Log.WithFields(logrus.Fields{
				"dstPath":          dstPath,
				"uploadingFileMd5": uf.FileMd5,
				"existingFileMd5":  tempMd5,
			}).Info("目标文件已经存在，且 MD5 和上传文件的 MD5 不一样，删除已存在的目标文件")

			os.Remove(dstPath)
		}
	}

	// [4] 如果目标目录不存在则创建。
	os.MkdirAll(uf.DstDir, os.ModePerm)

	// [5] 按照分片顺序合并分片到目标文件，合并成功后删除分片文件。
	chunkPaths := []string{}
	for _, chunk := range uf.Chunks {
		path := o.generateChunkPath(uf.Uid, chunk.Sn)
		chunkPaths = append(chunkPaths, path)
	}

	// 合并分片，err 为 nil 则合并成功，否则合并失败。
	log.Log.WithFields(logrus.Fields{
		"dstPath":    dstPath,
		"chunkCount": len(chunkPaths),
	}).Info("[开始] 合并分片为目标文件")

	err := utils.MergeFiles(dstPath, chunkPaths, uf.FileMd5)
	if err == nil {
		// 合并成功。
		o.updateUploadedFileState(uf, bean.US_Success)

		// 删除分片文件目录。
		o.removeChunkDir(uf.Uid)

		log.Log.WithFields(logrus.Fields{
			"dstPath":    dstPath,
			"chunkCount": len(chunkPaths),
		}).Info("[成功] 合并分片为目标文件")
	} else {
		// 合并失败。
		o.updateUploadedFileState(uf, bean.US_Failed)

		log.Log.WithFields(logrus.Fields{
			"dstPath": dstPath,
			"error":   err,
		}).Warn("[失败] 合并分片为目标文件错误")
	}
}

// updateUploadedFileState 更新上传文件的状态。
func (o *UploadService) updateUploadedFileState(uf *bean.UploadedFile, state int) {
	uf.State = state
	o.uploadRepo.UpdateUploadedFileState(uf.Uid, state)
}

// updateChunkState 更新文件分片的状态。
func (o *UploadService) updateChunkState(fileUid string, chunk *bean.UploadedFileChunk, state int) {
	chunk.State = state
	o.uploadRepo.UpdateChunkState(fileUid, chunk.Sn, state)
}

// needMerge 判断分片是否需要合并。
func (o *UploadService) needMerge(fileUid string) bool {
	// [1] 使用文件 Uid 查询上传文件对象。
	uf := o.uploadRepo.FindUploadedFile(fileUid)
	if uf == nil {
		return false
	}

	// [2] 如果还有分片没有上传完成则返回，所有分片都上传成功了则进行合并。
	for _, c := range uf.Chunks {
		if c.State != bean.US_Success {
			return false
		}
	}

	// [3] 所有分片都上传成功了则进行合并，如果正在合并中则返回。
	if uf.State == bean.US_Handling {
		return false
	} else {
		return true
	}
}

// removeChunkDir 删除文件分片所在目录。
func (o *UploadService) removeChunkDir(fileUid string) {
	os.RemoveAll(o.generateChunkDir(fileUid))
}
