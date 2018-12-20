package edu.mapper;

import edu.bean.UploadedFile;

/**
 * 一些常用，但是不成规模的数据库访问接口放这个文件里
 */
public interface CommonMapper {
    /**
     * 使用 ID 查询上传的文件
     *
     * @param id 上传的文件的 ID
     * @return 返回查找到的文件
     */
    UploadedFile findUploadedFileById(long id);

    /**
     * 插入或者更新上传的文件
     *
     * @param file 上传的文件
     */
    void insertOrUpdateUploadedFile(UploadedFile file);

    /**
     * 删除上传文件的记录
     *
     * @param id 上传的文件的 ID
     */
    void deleteUploadedFileById(long id);
}
