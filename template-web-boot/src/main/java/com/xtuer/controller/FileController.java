package com.xtuer.controller;

import com.xtuer.bean.Result;
import com.xtuer.bean.UploadedFile;
import com.xtuer.bean.Urls;
import com.xtuer.util.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 上传和访问文件的控制器
 *
 * 提示:
 *     获取文件名的时候没有使用 @PathVariable 是因为 SpringMVC 获取 URI 中最后一个部分时，如果此部分有点 . 则会有 Bug (获取带点 . 的中间部分没有问题)
 *     例如 pattern 为 /file/temp/{filename}，URI 为 /file/temp/1234.png，得到的 filename 为 1234 而不是 1234.png
 *
 *     当 URI 最后部分是文件名格式 (带点, 如 abc.png), 响应使用 @ResponseBody 不会生效, 而是继续寻找 view,
 *     这时 Ajax 的响应需要直接写到 response, content-type 设置为 application/json
 *
 *     访问临时文件和仓库中的文件，直接读取文件返回，不需要查询文件的原始名字，这样也可以支持从 nginx 访问
 *     下载文件时，先查询文件原来的名字，然后才读取文件返回，使用文件原来的名字保存比使用一个无意义的名字更友好
 */
@Controller
public class FileController extends BaseController {
    /**
     * 访问文件仓库中的文件
     *
     * 网址: http://localhost:8080/file/repo/2018-06-19/293591971581788160.docx
     * 参数: 无
     *
     * @throws IOException 读取文件出错时抛出异常
     */
    @GetMapping(Urls.URL_REPO_FILE)
    public void accessRepoFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 没有直接使用参数中的 filename 是因为 registered-suffixes-only 有 Bug，不能识别 .xml, .json 等后缀
        String uri = WebUtils.getUri(request);
        File file  = repoFileService.getRepoFileByUrl(uri);

        WebUtils.readFileToResponse(file, response);
    }

    /**
     * 下载文件仓库中的文件
     *
     * 网址: http://localhost:8080/file/download/2018-06-19/293591971581788160.docx
     * 参数: 无
     *
     * @throws IOException 读取文件出错时抛出异常
     */
    @GetMapping(Urls.URL_REPO_FILE_DOWNLOAD)
    public void downloadRepoFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 从下载的 url 中得到文件的仓库路径
        // 2. 获取仓库文件对象
        // 3. 查询上传时的文件信息，获取文件的原始名字
        // 4. 向 response 写入文件

        // [1] 从下载的 url 中得到文件的仓库路径
        // [2] 获取仓库文件对象
        String uri      = WebUtils.getUri(request);
        String repoPath = uri.substring(15); // "/file/download/" 后面的部分
        File   repoFile = repoFileService.getRepoFile(repoPath);

        // [3] 查询上传时的文件信息，获取文件的原始名字
        String originalName = null;           // 文件的原始名
        String uploadedName = repoFile.getName(); // 文件的原始名
        UploadedFile upFile = repoFileService.findUploadedFile(uploadedName);

        if (upFile != null) {
            originalName = upFile.getFilename();
        }

        // [4] 向 response 写入文件
        WebUtils.readFileToResponse(repoFile.getAbsolutePath(), originalName, request, response);
    }

    /**
     * 访问临时目录中的文件
     *
     * 网址: http://localhost:8080/file/temp/165694488704974848.docx
     * 参数: 无
     *
     * @throws IOException 读取文件出错时抛出异常
     */
    @GetMapping(Urls.URL_TEMP_FILE)
    public void accessTempFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filename = WebUtils.getUriFilename(request);
        File file = tempFileService.getTempFile(filename);

        WebUtils.readFileToResponse(file, response);
    }

    /**
     * 删除仓库中的文件
     *
     * 网址: http://localhost:8080/file/repo/2018-06-19/293591971581788160.docx
     * 参数: 无
     */
    @DeleteMapping(Urls.URL_REPO_FILE)
    public void deleteRepoFile(HttpServletRequest request, HttpServletResponse response) {
        String uri = WebUtils.getUri(request);
        File  file = repoFileService.getRepoFile(uri);
        repoFileService.deleteRepoFile(file, uri);

        // 没有返回 Result 是因为 Spring MVC 在处理带 . 的 URL 时返回对象，@ResponseBody 不能正确的处理
        WebUtils.ajaxResponse(response, Result.ok());
    }

    /**
     * 上传单个文件到临时文件夹
     *
     * 网址: http://localhost:8080/form/upload/temp/file
     * 参数: name: 上传表单里的 name 为 file: 如 <input name="file">
     *
     * @param file 上传的文件
     * @return 上传成功时 payload 为 UploadedFile，里面有上传的文件名和 URL，success 为 true，上传出错时抛异常
     * @throws IOException 保存文件出错时抛出异常
     */
    @PostMapping(Urls.FORM_UPLOAD_TEMP_FILE)
    @ResponseBody
    public Result<UploadedFile> uploadFileToTemp(@RequestParam("file") MultipartFile file) throws IOException {
        long userId = super.getCurrentUserId();
        UploadedFile result = tempFileService.uploadFileToTemp(file, userId);
        return Result.ok(result);
    }

    /**
     * 上传多个文件到临时文件夹
     *
     * 网址: http://localhost:8080/form/upload/temp/files
     * 参数: name: 上传表单里的 name 为 files: 如 <input name="files">
     *
     * @param files 上传的文件
     * @return 上传成功时 payload 为 List<UploadedFile>，里面有上传的文件名和 URL，success 为 true，上传出错时抛异常
     * @throws IOException 保存文件出错时抛出异常
     */
    @PostMapping(Urls.FORM_UPLOAD_TEMP_FILES)
    @ResponseBody
    public Result<List<UploadedFile>> uploadFilesToTemp(@RequestParam("files") List<MultipartFile> files) throws IOException {
        long userId = super.getCurrentUserId();
        List<UploadedFile> upFiles = new LinkedList<>();

        for (MultipartFile file : files) {
            upFiles.add(tempFileService.uploadFileToTemp(file, userId));
        }

        return Result.ok(upFiles);
    }
}
