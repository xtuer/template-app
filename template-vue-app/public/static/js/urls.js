const Urls = {
    // 用户
    API_USERS_BY_ID          : '/api/users/{userId}',           // 指定 ID 的用户
    API_USERS_NICKNAMES      : '/api/users/{userId}/nicknames', // 用户的昵称
    API_USERS_AVATARS        : '/api/users/{userId}/avatars',   // 用户的头像 URL
    API_USERS_GENDERS        : '/api/users/{userId}/genders',   // 用户性别
    API_USERS_MOBILES        : '/api/users/{userId}/mobiles',   // 用户手机
    API_USERS_PASSWORDS      : '/api/users/{userId}/passwords', // 用户密码
    API_USERS_PASSWORDS_RESET: '/api/users/{userId}/passwords/reset', // 重置密码
    API_USERS_CURRENT        : '/api/login/users/current',            // 当前登录的用户

    FORM_UPLOAD_TEMPORARY_FILE : '/form/upload/temp/file',  // 上传一个临时文件
    FORM_UPLOAD_TEMPORARY_FILES: '/form/upload/temp/files', // 上传多个临时文件
    API_CAN_PREVIEW_FILE_PREFIX: '/api/canPreview',         // 请求是否可预览文件的前缀

    // 消息系统
    MESSAGE_WEBSOCKET_URL: `ws://${window.location.hostname}:3721`,
};
