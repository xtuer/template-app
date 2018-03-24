package ebag.controller;

import ebag.bean.Result;
import ebag.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SchoolController {
    /**
     * 查找域名对应的学校的 ID:
     *     1. 如果参数 host 不空，则查询它对应的学校的域名
     *     2. 如果参数 host 为空，则查询 request 的域名对应的学校
     * URL: http://localhost:8080/api/schoolId
     *      http://localhost:8080/api/schoolId?host=hxdd.ebag.com
     * @param host // 域名
     * @return Payload 为学校的 ID
     */
    @GetMapping(Urls.API_SCHOOLS_ID)
    @ResponseBody
    public Result<String> schoolId(@RequestParam(required = false) String host, HttpServletRequest request) {
        host = StringUtils.isBlank(host) ? WebUtils.getHost(request) : host;

        // TODO

        return Result.ok(host);
    }

    /**
     * 使用学校的 ID 查询学校信息
     * URL: http//localhost:8080/api/schools/{schoolId}
     *
     * @param schoolId 学校的 ID
     * @return Payload 为学校
     */
    @GetMapping(Urls.API_SCHOOLS_BY_ID)
    @ResponseBody
    public Result<?> findSchoolById(@PathVariable Long schoolId) {
        return Result.ok();
    }

    /**
     * 创建或者更新学校: 如果学校对象的 ID 为 0 则创建，否则更新
     * URL: http//localhost:8080/api/schools
     *
     * @return Payload 为学校的 ID
     */
    @PostMapping(Urls.API_SCHOOLS)
    @ResponseBody
    public Result<Long> createSchool() {
        return Result.ok(1L);
    }
}
