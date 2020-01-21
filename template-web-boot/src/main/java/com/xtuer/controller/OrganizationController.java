package com.xtuer.controller;

import com.xtuer.bean.Organization;
import com.xtuer.bean.Page;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.mapper.OrganizationMapper;
import com.xtuer.service.OrganizationService;
import com.xtuer.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 操作机构控制器信息
 */
@RestController
public class OrganizationController extends BaseController {
    @Autowired
    private OrganizationService orgService;

    @Autowired
    private OrganizationMapper orgMapper;

    /**
     * 使用机构 ID 查询机构，同时会查询出机构的管理员
     *
     * 网址: http://localhost:8080/api/orgs/{orgId}
     * 参数: 无
     *
     * @param orgId 机构 ID
     * @return payload 为查询到的机构
     */
    @GetMapping(Urls.API_ORGS_BY_ID)
    public Result<Organization> findOrganization(@PathVariable long orgId) {
        Organization org = orgService.findOrganization(orgId);
        return Result.single(org, "ID 为 " + orgId + " 的机构不存在");
    }

    /**
     * 使用机构名称模糊查询查询机构
     *
     * 网址: http://localhost:8080/api/orgs?name=测试机构
     * 参数:
     *      name       [可选]: 机构名，可模糊查询，没有时查询所有机构
     *      pageNumber [可选]: 页码
     *      pageSize   [可选]: 数量
     *
     * @param name 机构名 (部分)
     * @param pageNumber 页码
     * @param pageSize   数量
     */
    @GetMapping(Urls.API_ORGS)
    public Result<List<Organization>> findOrganizations(@RequestParam(required = false) String name,
                                                        @RequestParam(required = false, defaultValue = "1") int pageNumber,
                                                        @RequestParam(required = false, defaultValue = "20") int pageSize) {
        return Result.ok(orgMapper.findOrganizationsLikeName(name, Page.of(pageNumber, pageSize)));
    }

    /**
     * 新增或更新机构，机构的数据使用 JSON 格式存储到 request body 中
     *
     * 网址: http://localhost:8080/api/orgs/{orgId}
     * 参数: 无
     *
     * @param organization  机构新增信息
     * @param bindingResult 统一校验
     */
    @PutMapping(Urls.API_ORGS_BY_ID)
    public Result<String> upsertOrganization(@RequestBody @Valid Organization organization, BindingResult bindingResult) {
        // 如果校验失败，返回失败信息给前端
        if(bindingResult.hasErrors()){
            return Result.fail(Utils.getBindingMessage(bindingResult));
        }

        return orgService.upsertOrganization(organization);
    }

    /**
     * 启用禁用机构
     *
     * 网址: http://localhost:8080/api/orgs/{orgId}/enabled?value=true
     * 参数: value 为 boolean 值，为 true 是启用机构，为 false 时禁用机构
     *
     * @param orgId 机构 ID
     * @param value 启用禁用的 boolean 值
     */
    @PutMapping(Urls.API_ORGS_ENABLE)
    public Result<String> enableOrganization(@PathVariable long orgId, @RequestParam boolean value) {
        orgService.enableOrganization(orgId, value);
        return Result.ok();
    }

    /**
     * 使用机构 ID 删除机构
     *
     * 网址: http://localhost:8080/api/orgs/{orgId}
     * 参数: 无
     *
     * @param orgId 机构 ID
     */
    @DeleteMapping(Urls.API_ORGS_BY_ID)
    public Result<String> deleteOrganization(@PathVariable long orgId) {
        orgService.deleteOrganization(orgId);
        return Result.ok();
    }

}
