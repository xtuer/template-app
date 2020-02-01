package com.xtuer.service;

import com.xtuer.bean.UploadedFile;
import com.xtuer.config.AppConfig;
import com.xtuer.bean.Urls;
import com.xtuer.mapper.FileMapper;
import com.xtuer.util.Utils;
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

import java.io.File;

/**
 * <pre>
 * 仓库文件服务，用于移动文件到文件仓库，访问仓库文件。
 * 主要接口:
 *     获取仓库文件: getRepoFileByUrl(repoUrl)
 *     移动临时文件到文件仓库: moveTempFileToRepo(tempUrl, [dirs])
 *     移动 HTML 中的临时文件到文件仓库: moveTempFileToRepoInHtml(html, [dirs])
 *
 *
 * 临时文件夹 ${uploadDirectory}，用于存储临时文件，里面的文件会定期删除
 * 仓库文件夹 ${repoDirectory}，  用于存储仓库文件，删除由业务逻辑来决定
 *
 * 提示: 为了简化命名，下面的 url 都是指 uri，即 http://www.xtuer.com/api/files/12345.png?stamp=2
 * 中的 /api/files/12345.png，去掉协议、域名、参数剩下的部分。
 *
 * 仓库文件的 url: 以 /file/repo/ 开头，中间可以有多级文件夹，最后是文件名: /file/repo/{dir1/dir2/dir3}/{filename}:
 *     A. /file/repo/2018-04-10/165694386577866752.jpg
 *     B. /file/repo/paper/268242114298118000/168242114298118144.png
 *
 * 仓库路径: 相对于文件仓库的相对路径:
 *     A. /file/repo/2018-04-10/165694386577866752.jpg 的 repoPath 为 2018-04-10/165694386577866752.jpg
 *     B. /file/repo/2018/04/10/165694386577866752.jpg 的 repoPath 为 s2018/04/10/165694386577866752.jpg
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
 * </pre>
 */
@Slf4j
@Service
public class RepoFileService {
    @Autowired
    private AppConfig config;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private TempFileService tempFileService;

    /**
     * 移动 url 对应的临时文件到文件仓库中，文件在仓库里的目录由 dirs 指定，如果 dirs 为空则目录名为当前日期。
     *
     * 提示:
     * 1. 临时文件名格式为 {long-number}[.ext] (上传得到的)
     * 2. 只有 uri 指向临时文件时才会移动到文件仓库并返回文件对应的仓库 url，其他情况返回原来的 url
     *    (调用此函数的地方不需要判断是否临时文件，内部已经进行了判断)
     *
     * 处理逻辑:
     * 1. 如果 url 是临时文件的 url 则进行移动
     *    A. 临时文件不存在返回 null
     *    B. 移动出错如操作文件失败返回 null
     *    C. 移动成功返回对应仓库文件的 url
     * 2. 如果 url 不是临时文件的 url 则不进行移动，直接返回原 url
     *    A. 如果 url 是仓库文件的 url 则不移动，如 /file/repo/2018-04-10/165694386577866752.jpg
     *    B. 如果 url 是完整网址的 url 则不移动，如 http://www.training.com/fox.png
     *
     * @param url  临时文件的 url (文件名为数字.suffix)
     * @param dirs 文件夹名的数组
     *
     * @return 返回移动文件到仓库后得到的 url 或者非临时文件返回原 url
     */
    public String moveTempFileToRepo(String url, String... dirs) {
        url = StringUtils.trim(url);

        if (tempFileService.isTempFileUrl(url)) {
            // 1. url 指向临时文件时移动对应的临时文件到文件仓库
            File tempFile = tempFileService.getTempFile(FilenameUtils.getName(url));
            return this.moveFileToRepo(tempFile, dirs);
        } else {
            // 2. url 指向仓库文件，或者是一个第三方的 url 则返回原 url
            log.info("非临时文件不移动到仓库: {}", url);
            return url;
        }
    }

    /**
     * 移动 html 中的图片、链接、音频、视频引用的临时文件到文件仓库中，移动后得到的 url 替换原来的 url (如果引用的不是临时文件，则不改变它们的 url)。
     * 文件在仓库里的目录由 dirs 指定，如果 dirs 为空则目录名为当前日期。
     *
     * @param html 要处理的 html
     * @param dirs 文件夹名的数组
     * @return 返回处理后的 html
     */
    public String moveTempFileToRepoInHtml(String html, String... dirs) {
        // 移动 html 中的图片、链接、音频、视频引用的临时文件到文件仓库中:
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
            src = this.moveTempFileToRepo(src, dirs);
            img.attr("src", src);
        }

        // [3] 获取所有的 <a>，把 href 引用的临时文件移动到文件仓库中
        for (Element a : doc.select("a")) {
            String href = a.attr("href");
            href = this.moveTempFileToRepo(href, dirs);
            a.attr("href", href);
        }

        // [4] 获取所有的 <source> (audio, video)，把 src 引用的临时文件移动到文件仓库中
        // <video width="320" height="240" controls>
        //     <source src="/file/temp/movie.mp4" type="video/mp4">
        // </video>
        for (Element mp3 : doc.select("source")) {
            String src = mp3.attr("src");
            src = this.moveTempFileToRepo(src, dirs);
            mp3.attr("src", src);
        }

        doc.outputSettings().prettyPrint(false);

        return doc.body().html();
    }

    /**
     * 移动文件 srcFile 到文件仓库，文件在仓库中的文件夹由 destDirs 指定:
     *     1. 多级文件夹: /repo/paper/123/x.png
     *     2. 如果 destDirs 为空则文件夹名为当前日期: /repo/2019-10-12/x.png
     *
     * @param srcFile  被移动的文件
     * @param destDirs 文件夹名的数组
     * @return 返回文件对应的 url，如果移动时发生异常，例如文件不存在，则返回 null
     */
    public String moveFileToRepo(File srcFile, String... destDirs) {
        // 1. 计算仓库文件的 url，例如为 /file/repo/2018-05-03/165694386577866752.doc
        // 2. 移动文件到仓库中
        // 3. 更新文件的 url 到数据库

        long   fileId   = getFileId(srcFile.getName());
        String filename = srcFile.getName();
        String repoPath = this.getRepoPath(filename, destDirs);
        File   repoFile = this.getRepoFile(repoPath);
        String repoUrl  = Urls.URL_REPO_FILE_PREFIX + repoPath;

        try {
            log.info("[开始] 移动文件 {} 到文件仓库 {}", srcFile.getAbsolutePath(), repoFile.getAbsolutePath());

            FileUtils.moveFile(srcFile, repoFile); // [2] 移动文件到仓库中
            fileMapper.updateUploadedFileUrlAndType(fileId, repoUrl , UploadedFile.PLATFORM_FILE); // [3] 更新文件的 URL 到数据库

            log.info("[结束] 移动文件 {} 到文件仓库 {}", srcFile.getAbsolutePath(), repoFile.getAbsolutePath());

            return repoUrl ;
        } catch (Exception e) {
            log.warn("[结束] 移动文件异常: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 获取传入的文件名 filename 的仓库路径，dirs 为文件夹名的数组，如果为空则 dirs 为当前日期，如 2019-10-11
     *
     * @param filename 文件名
     * @param dirs     文件夹名的数组
     * @return 返回
     */
    public String getRepoPath(String filename, String... dirs) {
        String repoPath = null;

        if (dirs.length == 0) {
            repoPath = Utils.today() + "/" + filename;
        } else {
            repoPath = String.join("/", dirs) + "/" + filename;
        }

        return repoPath;
    }

    /**
     * 使用文件仓库的相对路径获取文件
     *
     * @param repoPath 文件仓库的相对路径
     * @return 返回获取到的文件
     */
    public File getRepoFile(String repoPath) {
        return new File(config.getRepoDirectory(), repoPath);
    }

    /**
     * 获取 url 在文件仓库中的文件
     *
     * @param url 文件的 url
     * @return 返回仓库中的文件
     */
    public File getRepoFileByUrl(String url) {
        if (!this.isRepoFileUrl(url)) {
            log.info("不是仓库文件的 URL: {}", url);
            return null;
        }

        String repoPath = url.substring(11); // 去掉 /file/repo/ 得到的剩余部分
        return this.getRepoFile(repoPath);
    }

    /**
     * 判断传入的 url 是否仓库文件的 url
     *
     * @param url 文件的 url
     * @return 如果 url 以 /file/repo/ 开头说明是临时文件的 url 则返回 true，否则返回 false
     */
    public boolean isRepoFileUrl(String url) {
        return StringUtils.startsWith(url, Urls.URL_REPO_FILE_PREFIX);
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
     * 使用文件名查询上传的文件信息
     *
     * @param filename 文件名
     * @return 返回查询到的文件信息
     */
    public UploadedFile findUploadedFile(String filename) {
        return fileMapper.findUploadedFileById(getFileId(filename));
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
}
