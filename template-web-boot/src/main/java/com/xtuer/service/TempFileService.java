package com.xtuer.service;

import com.alibaba.fastjson.JSON;
import com.xtuer.bean.UploadedFile;
import com.xtuer.config.AppConfig;
import com.xtuer.bean.Urls;
import com.xtuer.mapper.FileMapper;
import com.xtuer.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * <pre>
 * 临时文件服务，用于上传文件到临时文件夹，访问临时文件。
 * 临时文件夹 ${uploadDirectory}，用于存储临时文件，里面的文件会定期删除
 *
 * 文件上传逻辑:
 *     1. 上传文件到临时文件夹
 *     2. 把文件从临时文件夹移动到文件仓库，并得到对应的 url
 *     3. 定时删除临时文件夹中 1 小时前创建的文件 (1 小时都还没使用，说明已经不再需要)
 *
 * 临时文件的 url: 以 /file/temp/ 开头，然后是文件名: /file/temp/{filename}:
 *     A. /file/temp/165694386577866752.jpg
 *     B. /file/temp/165694386577866752.png
 *
 * 注意:
 *     1. 上传文件名的格式为 {long-number}.[ext]，long-number 是使用 IdWorker 生成的 64 位的 long 类型整数
 *     2. 因为 url 比 uri 更好记，此文件中如果特殊说明，url 则代表 uri，也就是没有 host, port，protocol 等部分
 * </pre>
 */
@Slf4j
@Service
public class TempFileService extends BaseService {
    @Autowired
    private AppConfig config;

    @Autowired
    private FileMapper fileMapper;

    /**
     * 使用文件名获取临时文件
     *
     * @param filename 文件名
     * @return 返回临时文件对象
     */
    public File getTempFile(String filename) {
        return new File(config.getUploadDirectory(), filename);
    }

    /**
     * 判断传入的 url 是否临时文件的 url
     *
     * @param url 文件的  url
     * @return 如果 url 以 /file/temp/ 开头说明是临时文件的 url 则返回 true，否则返回 false
     */
    public boolean isTempFileUrl(String url) {
        return StringUtils.startsWith(url, Urls.URL_TEMP_FILE_PREFIX);
    }

    /**
     * 上传文件到临时目录
     *
     * @param file   上传的文件
     * @param userId 上传文件的用户 ID
     * @return 返回上传的文件信息对象
     * @throws IOException 保存文件到临时目录出错时抛出 IO 异常
     */
    public UploadedFile uploadFileToTemp(MultipartFile file, long userId) throws IOException {
        // 1. 为文件生成一个唯一 ID
        // 2. 获取原始文件后和缀名
        // 3. 临时文件名: 文件的 ID + 后缀名组合出上传保存的文件名，如 165694386577866752.png
        // 4. 计算保存的文件路径和 URL
        // 5. 保存上传的文件到临时文件目录
        // 6. 如果上传的是图片，则还要读取图片的宽和高
        // 7. 插入文件 ID、原始文件名到数据库
        // 8. 返回上传结果

        long   fileId             = nextId();
        String originalFilename   = file.getOriginalFilename();
        String extension          = FilenameUtils.getExtension(originalFilename); // 上传的文件的后缀名
        String tempFilename       = fileId + (StringUtils.isBlank(extension) ? "" : "." + extension); // 临时文件名
        File   tempFile           = getTempFile(tempFilename); // 临时文件
        String tempUrl            = Urls.URL_TEMP_FILE_PREFIX + tempFilename; // 临时文件的 URI: /file/temp/165694386577866752.png
        UploadedFile uploadedFile = new UploadedFile(fileId, originalFilename, tempUrl, UploadedFile.TEMPORARY_FILE, userId); // 上传的文件

        log.info("[开始] 上传文件 {}", originalFilename);
        log.info("[进行] 创建临时文件 {}", tempFile.getAbsolutePath());

        // [5] 保存上传的文件到临时目录 (目录不存在会自动创建)
        FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);

        // [6] 如果上传的是图片，则还要读取图片的宽和高
        if (Utils.isImage(tempFilename)) {
            Dimension size = Utils.getImageSize(tempFile.getAbsolutePath());

            if (size != null) {
                uploadedFile.setImageWidth((int) size.getWidth());
                uploadedFile.setImageHeight((int) size.getHeight());
            }
        }

        // [7] 插入文件 ID、原始文件名到数据库
        fileMapper.upsertUploadedFile(uploadedFile);

        log.info("[结束] 上传文件 {}\n{}", originalFilename, JSON.toJSONString(uploadedFile, true));

        // [8] 返回上传结果
        return uploadedFile;
    }
}
