package com.edu.training.service;

import com.alibaba.fastjson.JSON;
import com.edu.training.controller.Urls;
import com.edu.training.bean.UploadedFile;
import com.edu.training.config.AppConfig;
import com.edu.training.mapper.FileMapper;
import com.edu.training.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件工具，用于读写临时文件和文件仓库中的文件，相关文件夹有:
 *     临时文件夹 ${uploadDirectory}，用于存储临时文件，里面的文件会定期删除
 *     仓库文件夹 ${repoDirectory}，  用于存储文件仓库中的文件，删除由业务逻辑来决定
 *
 * 文件上传逻辑:
 *     1. 上传文件到临时文件夹
 *     2. 把文件从临时文件夹移动到文件仓库，并得到对应的 url
 *     3. 定时删除临时文件夹中 1 小时前创建的文件 (1 小时都还没使用，说明已经不再需要)
 *
 * 表单处理逻辑:
 *     1. 前端使用富文本编辑器异步上传文件到临时文件夹 ${uploadDirectory}，每个上传的文件都有一个对应的 url 如 tempFileUrl
 *     2. 前端提交表单的 html
 *     3. 服务器端解析 html 得到文件的 url，调用 moveFileToRepo() 把 url 指向的临时文件从临时文件夹 ${uploadDirectory}
 *        移动到仓库的文件夹 ${repoDirectory}/{date}，如果 url 没有指向临时文件则不进行移动，最后替换 html 里临时文件的 url 为最终的 url
 *        提示:
 *            a. 文件按日期 yyyy-MM-dd 分文件夹存储，每个文件有一个 url，此 url 和文件信息保存到数据库
 *            b. 每个目录下存放的文件或文件夹不宜过多，不超过 2 万个时性能还是很高的，2 万天有 55 年，所以按天存储文件足够使用
 *            c. 每个上传的文件系统都会为其分配一个不重复的文件名，格式为 {long-number}.[ext]
 *            d. 调用 moveFileToRepoInHtml() 即可
 *
 * 其他函数:
 *     获取临时文件: getTempFile()
 *     获取仓库文件: getRepoFile()
 *     删除仓库文件: deleteRepoFile()
 *
 * 注意:
 *     1. 上传文件名的格式为 {long-number}.[ext]，long-number 是使用 IdWorker 生成的 64 位的 long 类型整数
 *     2. 因为 URL 比 URI 更好记，此文件中如果特殊说明，URL 则代表 URI，也就是没有 host, port，protocol 等部分
 */
@Service
@Slf4j
public class FileService extends BaseService {
    // 仓库文件 URL 的 pattern: /file/repo/{date}/{filename}
    // 文件的 URL：/file/repo/2018-04-10/168242114298118144.doc
    // 图片的 URL：/file/repo/2018-04-10/165694386577866752.jpg
    public static final Pattern REPO_FILE_URL_PATTERN = Pattern.compile("/file/repo/(\\d{4}-\\d{2}-\\d{2})/(.+)");

    @Autowired
    private AppConfig config;

    @Autowired
    private FileMapper fileMapper;

    /**
     * 使用临时文件名获取临时文件
     *
     * @param filename 临时文件名
     * @return 返回临时文件对象
     */
    public File getTempFile(String filename) {
        return new File(config.getUploadDirectory(), filename);
    }

    /**
     * 使用文件名和日期目录名获取仓库中的文件
     *
     * @param filename 文件名
     * @param date     日期目录名
     * @return 返回仓库文件对象
     */
    public File getRepoFile(String filename, String date) {
        File repo = new File(config.getRepoDirectory(), date);
        File file = new File(repo, filename);

        return file;
    }

    /**
     * 使用仓库文件的 URI 获取对应的文件
     *
     * @param uri 文件的 URI
     * @return 返回仓库文件夹下的文件对象，如果 uri 的格式不对则返回 null
     */
    public File getRepoFile(String uri) {
        Matcher fileMatcher = REPO_FILE_URL_PATTERN.matcher(uri);

        String date     = null;
        String filename = null;

        if (fileMatcher.matches()) {
            date     = fileMatcher.group(1);
            filename = fileMatcher.group(2);
        }

        if (date == null || filename == null) {
            return null;
        }

        return getRepoFile(filename, date);
    }

    /**
     * 判断传入的 URI 是否临时文件的 URI
     *
     * @param uri 文件的  URI
     * @return 如果 URI 以 /file/temp/ 开头说明是临时文件的 URI 则返回 true，否则返回 false
     */
    public boolean isTempFileUri(String uri) {
        return StringUtils.startsWith(uri, Urls.URL_TEMP_FILE_PREFIX);
    }

    /**
     * 判断传入的 URI 是否仓库文件的 URI
     *
     * @param uri 文件的 URI
     * @return 如果 URI 匹配 REPO_FILE_URL_PATTERN 说明是仓库文件的 URI 则返回 true，否则返回 false
     */
    public boolean isRepoFileUri(String uri) {
        Matcher fileMatcher = REPO_FILE_URL_PATTERN.matcher(uri);
        return fileMatcher.matches();
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
        // 3. 文件的 ID + 后缀名组合出上传保存的文件名，如 165694386577866752.png
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

    /**
     * 移动 uri 对应的文件到文件仓库中，按照日期进行存储，返回此文件对应的正式 URI
     * 注意: 只有 uri 指向临时文件时才会移动文件 (调用此函数的地方不需要判断是否临时文件)，其他情况返回原来的 uri，不移动文件:
     * 1. 如果 uri 是临时文件的 URI 则进行移动
     *    1.1 临时文件不存在返回 null
     *    1.2 移动出错如操作文件失败返回 null
     *    1.3 移动成功返回文件对应的正式 URI
     * 2. 如果 uri 不是临时文件的 URI 则不进行移动，直接返回 uri，例如 URI 已经是仓库中文件的 URI 就不用再移动了，
     *    如果 uri 是一个第三方的 URL 如 http://www.training.com/fox.png 不用移动，因为在我们系统里没有对应的文件
     *
     * @param uri 文件的 URI
     * @return 返回移动文件后的 URI
     */
    public String moveFileToRepo(String uri) {
        uri = StringUtils.trim(uri);

        if (this.isTempFileUri(uri)) {
            // uri 指向临时文件则进行移动
            String today    = Utils.today();
            File   tempFile = this.getTempFile(FilenameUtils.getName(uri));
            String url      = this.moveFileToRepo(tempFile, today);

            return url;
        } else {
            // uri 指向仓库文件，或者是一个第三方完整的 URL 则直接返回
            log.info("非临时文件不移动到仓库: {}", uri);
            return StringUtils.trim(uri);
        }
    }

    /**
     * 移动文件 sourceFile 到仓库，文件按照日期进行存储，并返回访问文件的 URL
     *
     * @param sourceFile 被移动的文件
     * @param date       存储文件的日期目录
     * @return 返回文件对应的 URL，如果移动时发生异常，例如文件不存在，则返回 null
     */
    public String moveFileToRepo(File sourceFile, String date) {
        // 1. 计算访问文件的 URL，例如为 /file/repo/2018-05-03/165694386577866752.doc
        // 2. 移动文件到仓库中的文件夹
        // 3. 更新文件的 URL 到数据库
        long fileId     = getFileId(sourceFile.getName());
        String filename = sourceFile.getName();
        String finalUrl = Urls.URL_REPO_FILE_PREFIX + date + "/" + filename;
        File targetDirectory = new File(config.getRepoDirectory(), date);

        try {
            log.info("[开始] 移动文件 {} 到文件仓库 {}", sourceFile.getAbsolutePath(), targetDirectory.getAbsolutePath());

            FileUtils.moveFileToDirectory(sourceFile, targetDirectory, true); // [2] 移动文件到仓库中的文件夹
            fileMapper.updateUploadedFileUrlAndType(fileId, finalUrl, UploadedFile.PLATFORM_FILE); // [3] 更新文件的 URL 到数据库

            log.info("[结束] 移动文件 {} 到文件仓库 {}", sourceFile.getAbsolutePath(), targetDirectory.getAbsolutePath());

            return finalUrl;
        } catch (Exception e) {
            log.warn("[结束] 移动文件异常: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 移动 html 中的图片、链接、音频、视频引用的临时文件到文件仓库中，移动后得到的 url 替换原来的 url，
     * 如果引用的不是临时文件，则不改变它们的 url
     *
     * @param html 要处理的 html
     * @return 返回处理后的 html
     */
    public String moveFileToRepoInHtml(String html) {
        // 移动 html 中的图片、链接、音频、视频引用的临时文件到文件仓库中
        // 1. 解析 html 生成 document 对象
        // 2. 获取所有的 <img>，把 src 引用的临时文件移动到文件仓库中
        // 3. 获取所有的 <a>，把 href 引用的临时文件移动到文件仓库中
        // 4. 获取所有的 <source> (audio, video)，把 src 引用的临时文件移动到文件仓库中
        // 提示: 移动后得到的 url 替换原来的 url

        if (StringUtils.isBlank(html)) {
            return "";
        }

        // [1] 解析 html 生成 document 对象
        Document doc = Jsoup.parse(html);

        // [2] 获取所有的 <img>，把 src 引用的临时文件移动到文件仓库中
        for (Element img : doc.select("img")) {
            String src = img.attr("src");
            src = this.moveFileToRepo(src);
            img.attr("src", src);
        }

        // [3] 获取所有的 <a>，把 href 引用的临时文件移动到文件仓库中
        for (Element a : doc.select("a")) {
            String href = a.attr("href");
            href = this.moveFileToRepo(href);
            a.attr("href", href);
        }

        // [4] 获取所有的 <source> (audio, video)，把 src 引用的临时文件移动到文件仓库中
        // <video width="320" height="240" controls>
        //     <source src="/file/temp/movie.mp4" type="video/mp4">
        // </video>
        for (Element mp3 : doc.select("source")) {
            String src = mp3.attr("src");
            src = this.moveFileToRepo(src);
            mp3.attr("src", src);
        }

        doc.outputSettings().prettyPrint(false);

        return doc.body().html();
    }

    /**
     * 从文件仓库中删除文件 file
     *
     * @param file 要被删除的文件
     * @param url  要被删除的文件的 URL，用于输出信息，可以为 null
     */
    public void deleteRepoFile(File file, String url) {
        // 1. 删除文件
        // 2. 删除文件记录
        if (file == null) {
            return;
        }

        log.info("[开始] 删除仓库文件 {}，URL 为: {}", file.getAbsolutePath(), url);

        FileUtils.deleteQuietly(file);
        fileMapper.deleteUploadedFileById(getFileId(file.getName()));

        log.info("[结束] 删除仓库文件 {}", file.getAbsolutePath());
    }

    /**
     * 根据文件名获取文件的 ID，文件名格式为 {long-number}[.ext]
     *
     * @param filename 文件名
     * @return 返回文件的 ID，如果文件名格式不正确则返回 0
     */
    public long getFileId(String filename) {
        return NumberUtils.toLong(FilenameUtils.getBaseName(filename));
    }

    /**
     * 使用文件名查询上传的文件
     *
     * @param filename 文件名
     * @return 返回查询到的文件
     */
    public UploadedFile findUploadedFile(String filename) {
        return fileMapper.findUploadedFileById(getFileId(filename));
    }
}
