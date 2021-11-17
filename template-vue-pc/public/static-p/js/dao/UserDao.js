/**
 * 访问用户数据的 Dao
 */
export default class UserDao {
    /**
     * 获取当前页面登录的用户
     *
     * 网址: http://localhost:8080/api/login/users/current
     * 参数: 无
     *
     * @return {Promise} 返回 Promise 对象，resolve 的参数为用户对象，reject 的参数为错误信息
     */
    static findCurrentUser() {
        return Rest.get(Urls.API_USERS_CURRENT).then(({ data: user, success, message }) => {
            return Utils.response(user, success, message);
        });
    }

    /**
     * 查询 ID 为传入的 userId 的用户
     *
     * 网址: http://localhost:8080/api/users/{userId}
     * 参数: 无
     *
     * @param  {String}  userId 用户 ID
     * @return {Promise} 返回 Promise 对象，resolve 的参数为用户对象，reject 的参数为错误消息
     */
    static findUserById(userId) {
        return Rest.url(Urls.API_USERS_BY_ID).params({ userId }).get().then(({ data: user, success, message }) => {
            return Utils.response(user, success, message);
        });
    }

    /**
     * 更新用户的昵称、头像、手机、性别、密码。
     * 注意: 一次只能更新一个属性。
     *
     * 网址: http://localhost:8080/api/users/{userId}
     * 参数:
     *      nickname      [可选]: 昵称
     *      avatar        [可选]: 头像
     *      mobile        [可选]: 手机
     *      gender        [可选]: 性别 (0, 1, 2)
     *      oldPassword   [可选]: 旧密码
     *      newPassword   [可选]: 新密码
     *      renewPassword [可选]: 确认的密码
     *
     * 案例: UserDao.patchUser({ id: 1, nickname: 'Bob' })
     *
     * 1. 更新头像成功时，data 为头像的正式 URL
     * 2. 更新其他属性成功时 data 为空，message 为对应属性更新成功提示
     *
     * @return {Promise} 返回 Promise 对象，resolve 的参数为对于更新操作的结果，reject 的参数为错误信息
     */
    static patchUser(user) {
        return Rest.url(Urls.API_USERS_BY_ID).params({ userId: user.id }).data(user).patch()
            .then(({ data, success, message }) => {
                if (success) {
                    Message.success(message);
                    return Promise.resolve(data);
                } else {
                    Message.error(message);
                    return Promise.reject(message);
                }
            });
    }
}
