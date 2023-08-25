import { Rest } from '@/static/ts/axios.rest';
import { Urls } from '@/static/ts/urls';

/**
 * Api 示例。
 *
 * 泛型提示:
 * - 后端返回的是 List<Map<String, Object>>。
 * - TS 里需要使用 Array<{[key: string]: any}> 进行接收，不能使用 Array<Map<string, any>>。
 */
export default class DemoApi {
    /**
     * 查询传入 userId 用户的用户名。
     *
     * 网址: http://localhost:8080/api/demo/users/{userId}/username
     * 参数: 无
     * 测试: curl 'http://localhost:8080/api/demo/users/123/username'
     *
     * @param userId 用户 ID。
     * @returns 返回 Promise 的 resolve() 的参数为用户名，reject() 的参数为错误原因。
     */
    static async findUsername(userId: number): Promise<string> {
        return Rest.url(Urls.API_DEMO_USERS_NAME)
            .params({ userId })       // 路径参数
            .data({ filter: 'newdt' }) // 请求参数
            .get<string>()            // 发送请求
            .then(({ data: username, success, message }) => {
                return Rest.normalize({ data: username, success, message });
            });
    }
}
