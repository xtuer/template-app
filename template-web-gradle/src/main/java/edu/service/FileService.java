package edu.service;

import com.alibaba.fastjson.JSON;
import edu.bean.RedisKey;
import edu.bean.UploadedFile;
import edu.controller.Urls;
import edu.mapper.CommonMapper;
import edu.util.Utils;
import edu.util.WebUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具，用于读写临时文件和系统使用的数据文件，相关文件夹有:
 *     临时文件夹 ${tempUploadDirectory}，用于存储临时文件，里面的文件会定期删除
 *     数据文件夹 ${dataDirectory}，      用于存储系统正式使用的文件，删除由业务逻辑来决定
 *
 * 上传文件逻辑:
 *     1. 上传时先把文件保存到 ${tempUploadDirectory} 文件夹，每个上传到临时文件夹的文件都有一个对应的 URL 如 tempFileUrl
 *     2. 函数 readTemporaryFileToResponse() 读取 tempFileUrl 对应的文件响应给前端
 *     3. 最终提交数据的时候调用函数 moveFileToDataDirectory() 把 url 指向的临时文件
 *        从临时文件夹 ${tempUploadDirectory} 移动到数据文件夹 ${dataDirectory}，如果 url 没有指向临时文件则不进行移动，
 *        文件按日期 yyyy-MM-dd 分类存储 (2 万天有 55 年)，此时给这个文件生成正式的 URL 如 finalUrl，此 finalUrl 保存到数据库
 *     4. 函数 readDataFileToResponse() 读取 finalUrl 对应的文件响应给前端
 *
 * 其他函数:
 *     函数 deleteDataFileForUrl() 把 url 对应的文件从数据文件夹里删除
 *     函数 deleteTemporaryFileForName() 删除文件名对应的临时文件
 *
 * 注意:
 *     * 上传的文件名格式为 {id}[.ext]，id 是使用 IdWorker 生成的 64 位的 long 类型整数
 *     * 因为 URL 比 URI 更好记，此文件中如果特殊说明，URL 则代表 URI，也就是没有 host, port，protocol 等部分
 *     * 上传的文件访问时需要从数据库查询它的信息，例如上传的原始文件名，为了减少数据库的查询，所以访问时优先从 Redis 读取，没有才从数据库读取，
 *     *     缓存时间为 1 天，当修改文件信息后会删除 Redis 中的缓存
 */
@Service
public class FileService extends BaseService {
    private static Logger logger = LoggerFactory.getLogger(FileService.class);

    // 数据文件 URL 的 pattern
    // 文件的 URL：/file/data/2018-04-10/168242114298118144.doc
    // 图片的 URL：/file/data/2018-04-10/165694386577866752.jpg
    public static final Pattern DATA_FILE_URL_PATTERN = Pattern.compile("/file/(data)/(\\d{4}-\\d{2}-\\d{2})/(\\w+\\.\\w+)\\?*.*");

    @Value("${tempUploadDirectory}")
    private String tempUploadDirectory; // 上传文件的临时文件夹

    @Value("${dataDirectory}")
    private String dataDirectory; // 正式保存数据的文件夹

    @Autowired
    private CommonMapper commonMapper;

    private int cacheDuration = 86400; // 文件信息缓存 1 天

    /**
     * 保存上传的文件到临时文件夹，返回访问文件的临时 URL，如果上传的是图片，返回结果中还有图片的宽和高
     * 图片 URL: /file/temp/165694386577866752.png
     * 文件 URL: /file/temp/165694488704974848.docx
     *
     * @param file 上传的文件
     * @return 返回上传的文件的 URL，如果上传的是图片，返回结果中还有图片的宽和高
     * @throws IOException 保存文件出错时抛出异常
     */
    public UploadedFile uploadFileToTemporaryDirectory(MultipartFile file) throws IOException {
        // 1. 为文件生成一个不重复的 ID
        // 2. 获取原始文件后和缀名
        // 3. 文件的 ID + 文件后缀名组合出上传保存的文件名
        // 4. 计算保存的文件路径和 URL
        // 5. 保存上传的文件到临时文件目录
        // 6. 如果上传的是图片，则还要读取图片的宽和高
        // 7. 插入文件 ID、原始文件名到数据库
        // 8. 返回上传结果
        long   fileId             = generateId();
        String originalFilename   = file.getOriginalFilename();
        String extension          = FilenameUtils.getExtension(originalFilename); // 上传的文件的后缀名
        String tempFilename       = fileId + (StringUtils.isBlank(extension) ? "" : "." + extension); // 临时文件名
        File   tempFile           = new File(tempUploadDirectory, tempFilename);   // 临时文件
        String tempUrl            = Urls.URL_TEMPORARY_FILE_PREFIX + tempFilename; // 临时文件的 URL: /file/temp/165694386577866752.png
        UploadedFile uploadedFile = new UploadedFile(fileId, originalFilename, tempUrl, UploadedFile.TEMPORARY_FILE, getLoginUserId()); // 上传的文件

        logger.info("[开始] 上传文件 {}", originalFilename);
        logger.info("[进行] 创建临时文件 {}", tempFile.getAbsolutePath());

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
        insertOrUpdateUploadedFile(uploadedFile);

        logger.info("[结束] 上传文件 {}\n{}", originalFilename, JSON.toJSONString(uploadedFile, true));

        // [8] 返回上传结果
        return uploadedFile;
    }

    /**
     * 从临时文件夹读取文件到 HttpServletResponse
     *
     * @param filename 临时文件名
     * @param request  HttpServletRequest 对象
     * @param response HttpServletResponse 对象
     */
    public void readTemporaryFileToResponse(String filename, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 获取临时文件
        // 2. 获取临时文件上传时的名字
        // 3. 把临时文件写入 HttpServletResponse
        File   file = getTemporaryFileForFilename(filename);
        String path = file.getAbsolutePath();
        String originalFilename = getUploadedFilename(filename);

        originalFilename = (originalFilename != null) ? originalFilename : WebUtils.getUriFilename(WebUtils.getRequest());

        WebUtils.readFileToResponse(path, originalFilename, request, response);
    }

    /**
     * 从数据文件夹读取文件到 HttpServletResponse
     *
     * @param filename 文件名
     * @param date     文件保存的日期
     * @param request  HttpServletRequest 对象
     * @param response HttpServletResponse 对象
     */
    public void readDataFileToResponse(String filename, String date, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 获取数据文件对象
        // 2. 获取数据文件上传时的名字
        // 3. 把数据文件写入 HttpServletResponse
        File   file = getDataFile(filename, date);
        String path = file.getAbsolutePath();
        String originalFilename = getUploadedFilename(filename);

        WebUtils.readFileToResponse(path, originalFilename, request, response);
    }

    /**
     * 使用临时文件的 URI 获取对应的文件对象
     * 注意: 此 URI 应该是使用 HttpServletRequest 获取的，不带有 URL 后面的请求参数
     *
     * @param tempFileUri 临时文件的 URI
     * @return 返回临时文件对象
     */
    public File getTemporaryFile(String tempFileUri) {
        String filename = FilenameUtils.getName(tempFileUri);
        return new File(tempUploadDirectory, filename);
    }

    /**
     * 使用临时文件的名字获取对应的文件对象
     *
     * @param filename 临时文件名
     * @return 返回临时文件对象
     */
    public File getTemporaryFileForFilename(String filename) {
        return new File(tempUploadDirectory, filename);
    }

    /**
     * 使用数据文件或者图片的 URL 获取对应的文件对象
     *
     * @param url 文件的 URL
     * @return 返回数据文件夹下的文件对象
     */
    public File getDataFile(String url) {
        Matcher fileMatcher = DATA_FILE_URL_PATTERN.matcher(url);

        String date     = null;
        String filename = null;

        if (fileMatcher.matches()) {
            date     = fileMatcher.group(2);
            filename = fileMatcher.group(3);
        }

        if (date == null) {
            return null;
        }

        return getDataFile(filename, date);
    }

    /**
     * 获取数据文件
     *
     * @param filename 数据文件名
     * @param date     存储文件的日期
     * @return 返回文件对象
     */
    public File getDataFile(String filename, String date) {
        File dateDir = new File(dataDirectory, date);
        File file    = new File(dateDir, filename);

        return file;
    }

    /**
     * 删除临时文件
     *
     * @param filename 临时文件的 URL
     */
    public void deleteTemporaryFileForName(String filename) {
        // 1. 删除文件
        // 2. 删除文件的上传记录
        File file = getTemporaryFileForFilename(filename);

        logger.info("[开始] 删除临时文件: {}", file.getAbsolutePath());
        FileUtils.deleteQuietly(file);

        long fileId = getUploadedFileId(file.getName());
        this.deleteUploadedFileById(fileId);
        logger.info("[结束] 删除临时文件: {}", file.getAbsolutePath());
    }

    /**
     * 从数据文件夹删除 url 对应的文件
     *
     * @param url 文件对应的 URL
     */
    public void deleteDataFileForUrl(String url) {
        File file = getDataFile(url);
        this.deleteDataFile(file, url);
    }

    /**
     * 从数据文件夹删除文件
     *
     * @param filename 要被删除的文件名
     * @param date     存储文件的日期
     */
    public void deleteDataFile(String filename, String date) {
        File file = getDataFile(filename, date);
        this.deleteDataFile(file, null);
    }

    /**
     * 从数据文件夹删除文件 file
     *
     * @param file 要被删除的文件
     * @param url  要被删除的文件的 URL，用于输出信息，可以为 null
     */
    public void deleteDataFile(File file, String url) {
        // 1. 删除文件
        // 2. 删除文件的上传记录
        if (file == null) {
            return;
        }

        logger.info("[开始] 删除数据文件 {}，URL 为: {}", file.getAbsolutePath(), url);
        FileUtils.deleteQuietly(file);

        long fileId = getUploadedFileId(file.getName());
        this.deleteUploadedFileById(fileId);
        logger.info("[结束] 删除数据文件 {}", file.getAbsolutePath());
    }

    /**
     * 移动 uri 对应的文件到数据文件夹，根据移动的日期进行存储，返回此文件对应的正式 URI，
     * 也就是说，只有 uri 指向临时文件时才会移动文件 (外部不需要再判断是否临时文件了)，其他情况返回原来的 uri，不移动文件:
     * 1. 如果 uri 是临时文件的 URI 则进行移动
     *    1.1 临时文件不存在返回 null
     *    1.2 移动出错如操作文件失败返回 null
     *    1.3 移动成功返回文件对应的正式 URI
     * 2. 如果 uri 不是临时文件的 URI 则不进行移动，直接返回 uri，例如 URI 是正式文件的 URI 就用再移动了，
     *    如果 uri 是一个第三方的 URL 如 http://www.edu-edu.com/fox.png 就更不用移动了，在我们系统里没有对应的文件
     *
     * @param uri 文件的 URI
     * @return 返回移动文件后的 URI
     */
    public String moveFileToDataDirectory(String uri) {
        if (this.isTemporaryFileUri(uri)) {
            // uri 指向临时文件进行移动
            String today    = Utils.today();
            File   tempFile = this.getTemporaryFile(uri);
            String url      = this.moveFileToDataDirectory(tempFile, today);

            return url;
        } else {
            // uri 指向数据文件，或者是一个第三方完整的 URL 则直接返回
            return StringUtils.trim(uri);
        }
    }

    /**
     * 移动文件 sourceFile 到数据文件夹，文件按照日期进行存储，并返回访问文件的 URL
     *
     * @param sourceFile 被移动的文件
     * @param date       移动文件的日期
     * @return 返回文件对应的 URL，如果移动时发生异常，例如文件不存在，则返回 null
     */
    public String moveFileToDataDirectory(File sourceFile, String date) {
        // 1. 计算访问文件的 URL，例如为 /file/data/2018-05-03/165694386577866752.doc
        // 2. 移动文件到数据文件夹
        // 3. 更新上传文件的信息
        String filename = sourceFile.getName();
        String finalUrl = Urls.URL_DATA_FILE_PREFIX + date + "/" + filename;
        File targetDirectory = new File(dataDirectory, date);

        try {
            // [2] 移动文件到数据文件夹
            logger.info("[开始] 移动文件 {} 到数据文件夹 {}", sourceFile.getAbsolutePath(), targetDirectory.getAbsolutePath());
            FileUtils.moveFileToDirectory(sourceFile, targetDirectory, true);
            logger.info("[结束] 移动文件 {} 到数据文件夹 {}", sourceFile.getAbsolutePath(), targetDirectory.getAbsolutePath());

            // [3] 更新上传文件的信息为正式数据
            this.updateUploadedFileInfo(filename, finalUrl, UploadedFile.PLATFORM_FILE);

            return finalUrl;
        } catch (IOException e) {
            logger.warn("[结束] 移动文件异常: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 更新上传的文件的信息
     *
     * @param filename 文件名 (从文件名中提取出文件的 ID，这个文件名是系统生成带 ID 的文件名，不是文件的原始名字)
     * @param url      文件的 URL
     * @param type     文件的类型
     */
    public void updateUploadedFileInfo(String filename, String url, int type) {
        Long fileId = this.getUploadedFileId(filename);

        if (Utils.isValidId(fileId)) {
            UploadedFile uploadedFile = commonMapper.findUploadedFileById(fileId);

            if (uploadedFile != null) {
                uploadedFile.setUrl(url);
                uploadedFile.setType(type);
                this.insertOrUpdateUploadedFile(uploadedFile);
            }
        }
    }

    /**
     * 判断传入的 uri 是否临时文件的 URI
     *
     * @param uri 文件的  URI
     * @return 如果 uri 以 /file/temp/ 开头说明是临时文件的 URI，返回 true，否则返回 false
     */
    public boolean isTemporaryFileUri(String uri) {
        return StringUtils.startsWith(uri, Urls.URL_TEMPORARY_FILE_PREFIX);
    }

    /**
     * 判断传入的 uri 是否数据文件的 URI
     *
     * @param uri 文件的 URI
     * @return 如果 uri 匹配 DATA_FILE_URL_PATTERN 说明是临时文件的 URI，返回 true，否则返回 false
     */
    public boolean isDataFileUri(String uri) {
        Matcher fileMatcher = DATA_FILE_URL_PATTERN.matcher(uri);
        return fileMatcher.matches();
    }

    /**
     * 根据上传的文件的文件名获取文件的 ID，文件名格式为 {long number}[.ext]
     *
     * @param filename 上传的文件名
     * @return 文件的 ID，如果文件名不是上传的文件名格式，则返回 0
     */
    public long getUploadedFileId(String filename) {
        try {
            return Long.parseLong(FilenameUtils.getBaseName(filename));
        } catch (Exception ex) {
            logger.warn("文件名不是上传的格式 {long number}[.ext]: {}", filename);
            return 0;
        }
    }

    /**
     * 使用文件名查找上传的文件的原始名字
     *
     * @param filename 上传得到的文件名，格式为 217933208270929920.png 这样的格式
     * @return 返回上传的文件的原始名字，不存在则返回 null
     */
    public String getUploadedFilename(String filename) {
        Long fileId = getUploadedFileId(filename);
        UploadedFile uploadedFile = findUploadedFileById(fileId);

        if (uploadedFile != null) {
            return uploadedFile.getFilename();
        } else {
            logger.warn("文件没有上传记录: {}", filename);
            return null;
        }
    }

    /**
     * 使用 ID 查询上传的文件
     *
     * @param fileId 上传的文件的 ID
     * @return 返回查找到的文件
     */
    private UploadedFile findUploadedFileById(long fileId) {
        // 优先从缓存中读取上传文件的信息
        return super.getRedisDao().get(fileRedisKey(fileId),
                UploadedFile.class,
                () -> commonMapper.findUploadedFileById(fileId),
                cacheDuration);
    }

    /**
     * 插入或者更新上传的文件
     *
     * @param file 上传的文件
     */
    private void insertOrUpdateUploadedFile(UploadedFile file) {
        // 删除缓存并更新数据库记录
        deleteFileCache(file.getId());
        commonMapper.insertOrUpdateUploadedFile(file);
    }

    /**
     * 删除上传文件的记录
     *
     * @param fileId 上传的文件的 ID
     */
    private void deleteUploadedFileById(long fileId) {
        // 删除缓存并删除数据库记录
        deleteFileCache(fileId);
        commonMapper.deleteUploadedFileById(fileId);
    }

    /**
     * 上传文件的 Redis 缓存 key
     *
     * @param fileId 文件的 ID
     * @return 返回文件的 key
     */
    private String fileRedisKey(long fileId) {
        return RedisKey.UPLOADED_FILE + fileId;
    }

    /**
     * 删除文件在 Redis 中的缓存
     *
     * @param fileId 文件的 ID
     */
    private void deleteFileCache(long fileId) {
        super.getRedisDao().delete(fileRedisKey(fileId));
    }
}
