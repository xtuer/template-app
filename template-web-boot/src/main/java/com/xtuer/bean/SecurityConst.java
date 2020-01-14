package com.xtuer.bean;

/**
 * 安全相关的常量
 */
public interface SecurityConst {
    String AUTH_TOKEN_KEY      = "auth-token"; // 保存 token 的 key
    String SAVE_AUTH_TOKEN_KEY = "save-auth-token"; // 如果 header 里有 save-auth-token: true，则保存 token 到 cookie
    String LOGIN_USERNAME      = "username";   // 表单登录名字的 input name
    String LOGIN_PASSWORD      = "password";   // 表单登录密码的 input name
}
