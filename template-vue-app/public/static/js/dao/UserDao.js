export default class UserDao {
    /**
     * 请求当前登录用户
     * 网址: http://localhost:8080/api/login/users/current
     *
     * @param  {Function} callback 请求成功的回调函数，参数为登录用户
     * @return 无返回值
     */
    static findCurrentUser(callback) {
        Rest.get({ url: Urls.API_USERS_CURRENT }).then(result => {
            if (result.success) {
                const user = result.data;
                callback(user);
            } else {
                Utils.warning('还没有登录');
            }
        });
    }

    /**
     * 使用用户 ID 查询用户信息
     * 网址: http://localhost:8080/api/users/{userId}
     *
     * @param  {Long}     userId   用户的 ID
     * @param  {Function} callback 请求成功时的回调函数，参数为用户对象
     * @return 无返回值
     */
    static findUserById(userId, callback) {
        Rest.get({ url: Urls.API_USERS_BY_ID, pathVariables: { userId } }).then(result => {
            if (result.success) {
                let user = result.data;
                callback(user);
            } else {
                Utils.warning(result.message);
            }
        });
    }

    /**
     * 更新用户的性别
     * 网址: http://localhost:8080/api/users/{userId}/genders
     *
     * @param  {Long}     userId   用户的 ID
     * @param  {Integer}  gender   用户的性别
     * @param  {Function} callback 请求成功的回调函数，参数无
     * @return 无返回值
     */
    static updateUserGender(userId, gender, callback) {
        Rest.update({ url: Urls.API_USER_GENDERS, pathVariables: { userId }, data: { gender } }).then(result => {
            if (result.success) {
                callback();
            } else {
                Utils.warning(result.message);
            }
        });
    }

    /**
     * 更新用户的手机号
     * 网址: http://localhost:8080/api/users/{userId}/mobiles
     *
     * @param  {Long}     userId   用户的 ID
     * @param  {String}   mobile   用户的手机号
     * @param  {Function} callback 请求成功的回调函数，参数无
     * @return 无返回值
     */
    static updateUserMobile(userId, mobile, callback) {
        Rest.update({ url: Urls.API_USER_MOBILES, pathVariables: { userId }, data: { mobile } }).then(result => {
            if (result.success) {
                callback();
            } else {
                Utils.warning(result.message);
            }
        });
    }

    /**
     * 更新用户的密码
     * 网址: http://localhost:8080/api/users/{userId}/passwords
     *
     * @param  {Long}     userId        用户的 ID
     * @param  {String}   oldPassword   旧密码
     * @param  {String}   newPassword   新密码
     * @param  {String}   renewPassword 确认的新密码
     * @param  {Function} callback      请求成功的回调函数，参数无
     * @return 无返回值
     */
    static updateUserPassword(userId, oldPassword, newPassword, renewPassword, callback) {
        Rest.update({ url: Urls.API_USER_PASSWORDS, pathVariables: { userId }, data: { oldPassword, newPassword, renewPassword } }).then(result => {
            if (result.success) {
                callback();
            } else {
                Utils.warning(result.message);
            }
        });
    }

    /**
     * 更新用户的头像
     * 网址: http://localhost:8080/api/users/{userId}/avatars
     *
     * @param  {Long}   userId     用户的 ID
     * @param  {String} avatar     用户头像的临时 URL
     * @param  {Function} callback 请求成功的回调函数，参数为头像的正式 URL
     * @return 无返回值
     */
    static updateUserAvatar(userId, avatar, callback) {
        Rest.update({ url: Urls.API_USER_AVATARS, pathVariables: { userId }, data: { avatar } }).then(result => {
            if (result.success) {
                const finalAvatar = result.data; // 头像的正式 URL
                callback(finalAvatar);
            } else {
                Utils.warning(result.message);
            }
        });
    }

    /**
     * 更新用户昵称
     * 网址: http://localhost:8080/api/users/{userId}/nicknames
     *
     * @param  {Long}     userId   用户的 ID
     * @param  {String}   nickname 新昵称
     * @param  {Function} callback 请求成功的回调函数，参数无
     * @return 无返回值
     */
    static updateUserNickname(userId, nickname, callback) {
        Rest.update({ url: Urls.API_USER_NICKNAMES, pathVariables: { userId }, data: { nickname } }).then(result => {
            if (result.success) {
                callback();
            } else {
                Utils.warning(result.message);
            }
        });
    }

    /**
     * 重置用户的密码
     * 网址: http://localhost:8080/api/users/{userId}/passwords/reset
     *
     * @param {Long}     userId   用户的 ID
     * @param {Function} callback 请求成功的回调函数，参数无
     * @return 无返回值
     */
    static resetUserPassword(userId, callback) {
        Rest.update({ url: Urls.API_USER_PASSWORDS_RESET, pathVariables: { userId } }).then(result => {
            if (result.success) {
                callback();
            } else {
                Utils.warning(result.message);
            }
        });
    }
}
