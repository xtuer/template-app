package edu.controller;

import edu.bean.Result;
import edu.bean.User;
import edu.mapper.UserMapper;
import edu.service.IdWorker;
import edu.service.RedisDao;
import edu.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.Filter;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 示范使用的控制器
 */
@Controller
@Slf4j
public class DemoController {
    @Value("${maxUploadSize}")
    private long size;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisDao redis;

    @Autowired
    private FilterChainProxy filterChainProxy;

    // 网址: http://localhost:8080/page/demo/rest
    @RequestMapping(Urls.PAGE_DEMO_REST)
    public String toRestPage(ModelMap map) {
        map.put("action", "access demo page");
        return Urls.FILE_DEMO_REST;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //                                         REST 用法                                //
    // 读取: GET                                                                        //
    // 创建: POST                                                                       //
    // 更新: PUT                                                                        //
    // 删除: DELETE                                                                     //
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * REST 读取
     * 网址: http://localhost:8080/demo/rest/{id}?name=Alice
     * 参数: name
     *
     * @param id
     * @param name
     * @param map
     * @return
     */
    @GetMapping("/demo/rest/{id}")
    @ResponseBody
    public Result restGet(@PathVariable int id, @RequestParam String name, ModelMap map) {
        map.addAttribute("id", id);
        map.addAttribute("name", name);
        return Result.ok("GET handled", map);
    }

    /**
     * REST 读取
     * 网址: http://localhost:8080/demo/rest?name=Alice
     * 参数: name
     *
     * @param name
     * @return
     */
    @GetMapping("/demo/rest")
    @ResponseBody
    public Result restGet(@RequestParam String name) {
        return Result.ok("GET handled", name);
    }

    /**
     * REST 创建
     * 网址: http://localhost:8080/demo/rest
     * 参数: 无
     *
     * @return
     */
    @PostMapping("/demo/rest")
    @ResponseBody
    public Result restPost() {
        return new Result(true, "CREATE handled");
    }

    /**
     * REST 的更新
     * 网址: http://localhost:8080/demo/rest
     * 参数: name, age
     *
     * @param name
     * @param age
     * @return
     */
    @PutMapping("/demo/rest")
    @ResponseBody
    public Result restPut(@RequestParam String name, @RequestParam int age) {
        return new Result(true, "UPDATE handled", name + " : " + age);
    }

    /**
     * REST 删除
     * 网址: http://localhost:8080/demo/rest
     * 参数: 无
     *
     * @return
     */
    @DeleteMapping("/demo/rest")
    @ResponseBody
    public Result restDelete() {
        return new Result(true, "DELETE handled");
    }

    /**
     * REST 创建，处理 application/json 的请求
     * 网址: http://localhost:8080/demo/rest/requestBody
     * 参数: name
     *
     * @return
     */
    @PostMapping("/demo/rest/requestBody")
    @ResponseBody
    public Result restPostJsonRequestBody(@RequestBody String content) {
        return new Result(true, "CREATE requestBody handled: " + content);
    }

    /**
     * REST 更新，处理 application/json 的请求
     * 网址: http://localhost:8080/demo/rest/requestBody
     * 参数: name
     *
     * @return
     */
    @PutMapping("/demo/rest/requestBody")
    @ResponseBody
    public Result restUpdateJsonRequestBody(@RequestBody String content) {
        return new Result(true, "UPDATE requestBody handled: " + content);
    }

    /**
     * REST 删除，处理 application/json 的请求
     * 网址: http://localhost:8080/demo/rest/requestBody
     * 参数: name
     *
     * @return
     */
    @DeleteMapping("/demo/rest/requestBody")
    @ResponseBody
    public Result restDeleteJsonRequestBody(@RequestBody String content) {
        return new Result(true, "DELETE requestBody handled: " + content);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //                                      访问时发生异常                                //
    //////////////////////////////////////////////////////////////////////////////////////
    // http://localhost:8080/demo/exception
    @GetMapping("/demo/exception")
    public String exception() {
        throw new RuntimeException("普通访问发生异常");
    }

    /**
     * 字符串日期转换为日期 Date 对象，接收 2 种格式的字符串: yyyy-MM-dd 或者 yyyy-MM-dd HH:mm:ss
     * 网址: http://localhost:8080/demo/string-to-date?date=2017-03-12
     *      http://localhost:8080/demo/string-to-date?date=2017-03-12%2012:10:15
     * @param date
     * @return
     */
    @GetMapping("/demo/string-to-date")
    @ResponseBody
    public Result<Date> stringToDate(@RequestParam("date") Date date) {
        return Result.ok("日期转换", date);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //                                          文件上传                                 //
    //////////////////////////////////////////////////////////////////////////////////////
    // 网址: http://localhost:8080/form/demo/upload
    @PostMapping(Urls.FORM_DEMO_UPLOAD)
    @ResponseBody
    public Result uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam(required = false) String username,
                             @RequestParam(required = false) String password) throws IOException {
        log.debug(file.getOriginalFilename());
        // 不会自动创建文件夹，没有就报错，为了简单，可以使用 IOUtils 来处理
        // 会覆盖同名文件
        file.transferTo(new File("/Users/Biao/Desktop/" + file.getOriginalFilename()));
        return Result.ok("Username: " + username + ", Password: " + password + ", File: " + file.getOriginalFilename());
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //                                        参数验证                                   //
    //////////////////////////////////////////////////////////////////////////////////////
    // 网址:
    // http://localhost:8080/demo/validate
    // http://localhost:8080/demo/validate?username=bob
    // http://localhost:8080/demo/validate?username=bob&password=xxx
    @GetMapping("/demo/validate")
    @ResponseBody
    public Result<User> validateDemo(@Valid User demo, BindingResult bindingResult) {
        // 如有参数错误，则返回错误信息给客户端
        if (bindingResult.hasErrors()) {
            return Result.fail(Utils.getBindingMessage(bindingResult));
        }

        return Result.ok("", demo);
    }

    // 网址: http://localhost:8080/demo/uniqueName?username=ali
    @GetMapping("/demo/uniqueName")
    @ResponseBody
    public Result uniqueName(@RequestParam String username) {
        return Result.ok("", !"ali".equals(username));
    }

    // 网址: http://localhost:8080/demo/jsonp-test
    @GetMapping(value="/demo/jsonp-test", produces= Urls.JSONP_CONTENT_TYPE)
    @ResponseBody
    public String jsonpTest(@RequestParam String callback) {
        return Result.jsonp(callback, Result.ok("Congratulation", "Your data object"));
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //                                        使用配置                                   //
    //////////////////////////////////////////////////////////////////////////////////////
    // 网址: http://localhost:8080/demo/properties
    @GetMapping("/demo/properties")
    @ResponseBody
    public Result properties() {
        System.out.println("Size: " + size);
        return Result.ok(size);
    }

    /**
     * 获取 Spring Security 的 filters
     * 网址: http://localhost:8080/demo/filters
     */
    @GetMapping("/demo/filters")
    @ResponseBody
    public Map securityFilters() {
        Map<String, Map<String, String>> filterChains= new HashMap<>();
        int i = 1;

        for(SecurityFilterChain chain :  this.filterChainProxy.getFilterChains()){
            Map<String, String> filters = new HashMap<>();
            int j = 1;

            for(Filter filter : chain.getFilters()){
                filters.put("" + (j++), filter.getClass().getName());
            }

            filterChains.put("" + (i++), filters);
        }

        return filterChains;
    }

    /**
     * URL 里有参数，POST 的 body 里也有参数
     * 网址: http://localhost:8080/demo/post-with-params?username=Biao
     * Params: password=1234
     *
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/demo/post-with-params")
    @ResponseBody
    public String postWithParams(@RequestParam String username, @RequestParam String password) {
        return username + ", " + password;
    }

    /**
     * 读取资源文件夹下的文件
     * 网址: http://localhost:8080/demo/readFile
     *
     * @throws IOException
     */
    @GetMapping("/demo/readFile")
    @ResponseBody
    public Result readFile() throws IOException {
        File file = new File(getClass().getClassLoader().getResource("logback.xml").getFile());
        String content = FileUtils.readFileToString(file, "UTF-8");
        System.out.println(content);

        file = new File(new ClassPathResource("logback.xml").getURI());
        content = FileUtils.readFileToString(file, "UTF-8");
        System.out.println(content);

        return Result.ok();
    }

    /**
     * 测试前端传 undefined and null(可以使用 Chrome 查看请求的 Headers)，服务器端都认为没有传，会使用默认值:
     *     undefined 的参数不会放到 header 中: 没有默认值，抛异常
     *     null 的参数会传空: 没有默认值则认为是空字符串
     *
     * 网址: http://localhost:8080/demo/undefined-null
     *
     * @param un
     * @param nu
     */
    @GetMapping("/demo/undefined-null")
    @ResponseBody
    public Result undefinedNull(@RequestParam(defaultValue="Default-For-Undefined") String un,
                                @RequestParam(defaultValue="Default-For-Null") String nu) {
        System.out.println(un + ", " + nu);
        return Result.ok();
    }

    /**
     * 网址: http://localhost:8080/demo/new-id
     */
    @GetMapping("/demo/new-id")
    @ResponseBody
    public Result newId() {
        return Result.ok(idWorker.nextId());
    }

    /**
     * 使用 ID 查找用户
     * 网址: http://localhost:8080/api/demo/users/{id}
     */
    @GetMapping("/api/demo/users/{id}")
    @ResponseBody
    public Result<User> findUserById(@PathVariable Long id) {
        return Result.ok(userMapper.findUserById(id));
    }
}
