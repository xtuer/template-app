package ebag.controller;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import ebag.bean.Dict;
import ebag.bean.Result;
import ebag.mapper.DictMapper;
import ebag.service.IdWorker;
import ebag.util.PageUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Controller
public class DictController {
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private DictMapper dictMapper;

    /**
     * 查找指定类型下的字典，如果 type 为空或者 null，则查找所有的字典
     * URL: http://localhost:8080/api/dicts?type=学段&pageNumber=1&pageSize=30
     * 参数: type, pageNumber, pageSize 都是可选参数
     *
     * @param type       字典分类
     * @param pageNumber 页码
     * @param pageSize   每页数量
     */
    @GetMapping(Urls.API_DICTS)
    @ResponseBody
    public Result<Dict> findDictsByPage(@RequestParam(required = false) String type,
                                        @RequestParam(required = false, defaultValue = "1") int pageNumber,
                                        @RequestParam(required = false, defaultValue = "50") int pageSize) {
        int offset = PageUtils.offset(pageNumber, pageSize);
        List<Dict> dicts = dictMapper.findByType(type, offset, pageSize);
        return Result.ok(dicts);
    }

    /**
     * 查找的字典所有分类
     * URL: http://localhost:8080/api/dictTypes
     */
    @GetMapping(Urls.API_DICT_TYPES)
    @ResponseBody
    public Result<String> findDictTypes() {
        return Result.ok(dictMapper.findTypes());
    }

    /**
     * 导入 Excel xlsx 中的字典数据到据数据库
     *
     * @param file Excel 字典文件
     */
    @PostMapping(Urls.FORM_DICTS_IMPORT)
    @ResponseBody
    public Result importDicts(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 使用 EasyPOI 解析上传的文件 file 中的内容为 Dict 列表
            InputStream in = file.getInputStream();
            ImportParams params = new ImportParams();
            params.setImportFields(new String[]{"编码", "值", "分类"}); // Excel 中必须有这几个列，否则格式不正确抛异常
            List<Dict> dicts = ExcelImportUtil.importExcel(in, Dict.class, params);
            IOUtils.closeQuietly(in);

            // 2. 导入数据库
            for (Dict dict : dicts) {
                dict.setId(idWorker.nextId());
                dictMapper.insertOrUpdate(dict);
            }
        } catch (Exception ex) {
            return Result.fail(ExceptionUtils.getStackTrace(ex));
        }

        return Result.ok();
    }
}
