package com.xtuer.mapper;

import com.xtuer.bean.UploadedFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 上传文件的 Mapper
 */
@Mapper
public interface FileMapper {
    /**
     * 使用 ID 查询上传的文件
     *
     * @param id 文件 ID
     * @return 返回查找到的文件
     */
    UploadedFile findUploadedFileById(long id);

    /**
     * 插入或者更新上传的文件
     *
     * @param file 文件
     */
    void upsertUploadedFile(UploadedFile file);

    /**
     * 更新文件的 URL
     *
     * @param id   文件 ID
     * @param url  文件 URL
     * @param type 类型
     */
    void updateUploadedFileUrlAndType(long id, String url, int type);

    /**
     * 删除上传文件的记录
     *
     * @param id 文件 ID
     */
    void deleteUploadedFileById(long id);
}
