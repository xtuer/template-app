package edu.controller;

import edu.bean.Result;
import edu.bean.UploadedFile;
import edu.service.FileService;
import edu.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 上传和访问文件的控制器
 *
 * 提示:
 *     捕捉文件名的时候没有使用 @PathVariable 是因为 SpringMVC 捕捉 URI 中最后一个部分时，如果带有 . 则会有 Bug，捕捉中间部分没有问题
 *     例如 pattern 为 /file/temp/{filename}，URI 为 /file/temp/1234.png，得到的 filename 为 1234 而不是 1234.png
 *
 *     当 URI 最后部分是文件名格式 (带点, 如 abc.png), 响应使用 @ResponseBody 不会生效, 而是继续寻找 view,
 *     这时 Ajax 的响应需要直接写到 response, content-type 设置为 application/json
 */
@Controller
public class FileController {
    @Autowired
    private FileService fileService;

    /**
     * 访问数据文件夹中的文件
     * 网址: http://localhost:8080/file/data/2018-06-19/293591971581788160.docx
     *
     * @param date     存储的日期目录
     * @param request  HttpServletRequest 对象
     * @param response HttpServletResponse 对象
     * @throws IOException 读取文件出错时抛出异常
     */
    @GetMapping(Urls.URL_DATA_FILE)
    public void accessDataFile(@PathVariable String date, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 没有直接使用参数中的 filename 是因为 registered-suffixes-only 有 Bug，不能识别 .xml, .json 等后缀
        String filename = WebUtils.getUriFilename(request);
        fileService.readDataFileToResponse(filename, date, request, response);
    }

    /**
     * 访问临时文件中的文件
     * 网址: http://localhost:8080/file/temp/165694488704974848.docx
     *
     * @param request  HttpServletRequest 对象
     * @param response HttpServletResponse 对象
     * @throws IOException 读取文件出错时抛出异常
     */
    @GetMapping(Urls.URL_TEMPORARY_FILE)
    public void accessTemporaryFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filename = WebUtils.getUriFilename(request);
        fileService.readTemporaryFileToResponse(filename, request, response);
    }

    /**
     * 删除数据文件
     * 网址: http://localhost:8080/file/data/2018-06-19/293591971581788160.docx
     */
    @DeleteMapping(Urls.URL_DATA_FILE)
    public void deleteDataFile(HttpServletRequest request, HttpServletResponse response) {
        String uri = WebUtils.getUri(request);
        fileService.deleteDataFileForUrl(uri);

        // 没有返回 Result 是因为 Spring MVC 在处理带 . 的 URL 时返回对象，@ResponseBody 不能正确的处理
        WebUtils.ajaxResponse(response, Result.ok());
    }

    /**
     * 删除临时文件
     * 网址: http://localhost:8080/file/temp/165694488704974848.docx
     */
    @DeleteMapping(Urls.URL_TEMPORARY_FILE)
    public void deleteTemporaryFile(HttpServletRequest request, HttpServletResponse response) {
        String filename = WebUtils.getUriFilename(request);
        fileService.deleteTemporaryFileForName(filename);
        WebUtils.ajaxResponse(response, Result.ok());
    }

    /**
     * 上传单个文件到临时文件夹
     * 网址: http://localhost:8080/form/upload/temp/file
     *
     * @param file 上传的文件
     * @return 上传成功时 payload 为 UploadedFile，里面有上传的文件名和 URL，success 为 true，上传出错时抛异常
     * @throws IOException 保存文件出错时抛出异常
     */
    @PostMapping(Urls.FORM_UPLOAD_TEMPORARY_FILE)
    @ResponseBody
    public Result<UploadedFile> uploadTemporaryFile(@RequestParam("file") MultipartFile file) throws IOException {
        UploadedFile result = fileService.uploadFileToTemporaryDirectory(file);
        return Result.ok(result);
    }

    /**
     * 上传多个文件到临时文件夹
     * 网址: http://localhost:8080/form/upload/temp/files
     *
     * @param files 上传的文件
     * @return 上传成功时 payload 为 List<UploadedFile>，里面有上传的文件名和 URL，success 为 true，上传出错时抛异常
     * @throws IOException 保存文件出错时抛出异常
     */
    @PostMapping(Urls.FORM_UPLOAD_TEMPORARY_FILES)
    @ResponseBody
    public Result<List<UploadedFile>> uploadTemporaryFiles(@RequestParam("files") List<MultipartFile> files) throws IOException {
        List<UploadedFile> urs = new LinkedList<>();

        for (MultipartFile file : files) {
            urs.add(fileService.uploadFileToTemporaryDirectory(file));
        }

        return Result.ok(urs);
    }
}
