/**
 * 机构 Dao
 */
export default class OrgDao {
    /**
     * 使用机构名称模糊查询查询机构
     *
     * 网址: http://localhost:8080/api/orgs?name=测试机构
     * 参数:
     *      name       [可选]: 机构名，可模糊查询，没有时查询所有机构
     *      pageNumber [可选]: 页码
     *      pageSize   [可选]: 数量
     *
     * @param pageNumber 页码
     * @param pageSize   数量
     * @return {Promise} 返回 Promise 对象，resolve 的参数为机构数组，reject 的参数为错误信息
     */
    static findOrganizations(filter) {
        return Rest.url(Urls.API_ORGS).data(filter)
            .get()
            .then(({ data: orgs, success, message }) => {
                return Utils.response(orgs, success, message);
            });
    }

    /**
     * 启用禁用机构
     *
     * 网址: http://localhost:8080/api/orgs/{orgId}/enabled
     * 参数: value 为 boolean 值，为 true 是启用机构，为 false 时禁用机构
     *
     * @param orgId 机构 ID
     * @param value 启用禁用的 boolean 值
     * @return {Promise} 返回 Promise 对象，resolve 的参数为操作是否成功，reject 的参数为错误信息
     */
    static enableOrganization(orgId, enabled) {
        return Rest.url(Urls.API_ORGS_ENABLE).params({ orgId }).data({ value: enabled })
            .update()
            .then(({ data: ok, success, message }) => {
                return Utils.response(ok, success, message);
            });
    }

    /**
     * 新增或更新机构，机构的数据使用 JSON 格式存储到 request body 中
     *
     * 网址: http://localhost:8080/api/orgs/{orgId}
     * 参数: 无
     *
     * @param organization  机构新增信息
     * @return {Promise} 返回 Promise 对象，resolve 的参数为服务端返回的机构，reject 的参数为错误信息
     */
    static upsertOrganization(org) {
        return Rest.url(Urls.API_ORGS_BY_ID).params({ orgId: org.orgId }).data(org).json(true)
            .update()
            .then(({ data: respOrg, success, message }) => {
                return Utils.response(respOrg, success, message);
            });
    }
}
