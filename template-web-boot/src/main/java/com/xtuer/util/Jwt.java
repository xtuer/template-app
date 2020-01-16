package com.xtuer.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * 使用 JWT 的算法生成 token、验证 token 的有效性以及从 token 中提取数据。Token 中可包含用户数据、签名，能够防止 token 被篡改。
 *
 * 标准 JWT 生成的 token 由 3 部分组成，这里对其进行了简化，去掉了算法说明的部分，保留了数据和签名部分.
 * 参考: http://www.jianshu.com/p/576dbf44b2ae
 *
 * 需要注意的是，放到 token 里的数据不要太多，否则会使得 token 很大，而 token 有可能放在 cookie, header 中，
 * 如果过大，容易被截断导致 token 无效.
 *
 * 为什么不使用 com.auth0:java-jwt:3.3.0 实现的 JWT 呢？因为他的算法在 Nginx 端实现是不够方便。
 *
 * 使用方法:
 * 生成 token: Jwt.create(appId, appKey).param("username", "放下").expiredAt(System.currentTimeMillis() + 2000).token()
 * 校验 token: Jwt.checkToken(token, appKey)
 * 提取 token 中的用户数据: Jwt.params(token)
 */
public final class Jwt {
    /**
     * 检查 token 是否有效: 使用 payload 计算的签名结果和 token 中的签名一样，如果还存在有效期 expiredAt 并且未过期，则签名有效.
     *
     * @param jwtToken JWT token
     * @param appKey   应用的 Key
     * @return 签名有效返回 true，否则返回 false.
     */
    public static boolean checkToken(String jwtToken, String appKey) {
        if (StringUtils.isBlank(jwtToken)) {
            return false;
        }

        int dotIndex = jwtToken.indexOf(".");
        if (dotIndex == -1) {
            return false;
        }

        try {
            // 1. 解析出参数的 map params
            // 2. 如果 params 中存在 expiredAt，如果 expiredAt 超过当前时间则 token 过期无效
            // 3. 如果 token 未过期，则用 appKey+params 计算签名，如果和 signature 相等则签名有效
            Map<String, String> params = Jwt.params(jwtToken);

            try {
                // 检查签名是否过期
                long expiredAt = Long.parseLong(params.get("expiredAt"));
                if (expiredAt < System.currentTimeMillis()) {
                    return false;
                }
            } catch (NumberFormatException ex) {}

            String signature = jwtToken.substring(dotIndex+1);
            return signature.equals(Jwt.sign(params, appKey));
        } catch (JSONException ex) {
            return false;
        }
    }

    /**
     * 获取 token 中的 payload 的 map.
     *
     * @param jwtToken JWT token
     * @return 返回 payload 的 map，如果 token 无效则返回空的 map.
     */
    public static Map<String, String> params(String jwtToken) {
        int dotIndex = jwtToken.indexOf(".");
        if (dotIndex == -1) {
            return Collections.emptyMap();
        }

        try {
            String payload = Utils.unbase64UrlSafe(jwtToken.substring(0, dotIndex));
            return JSON.parseObject(payload, new TypeReference<TreeMap<String, String>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private static String sign(Map<String, String> params, String appKey) {
        // 初始化计算签名的字符串 signedText 为 appKey，
        // 然后按照 params 中 key 的字母序遍历 params，value 挨个的加在到 toSignedText 后面
        // 对 signedText 求 MD5
        Map<String, String> sortedMap = new TreeMap<>(params);
        StringBuilder signedText = new StringBuilder(appKey);

        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            signedText.append(entry.getValue());
        }

        return Utils.md5(signedText.toString());
    }

    /**
     * 使用 appId 和 appKey 创建一个 JWT 的 builder，然后使用此 builder 设置 payload 的参数计算 token.
     *
     * @param appId  应用的 ID
     * @param appKey 应用的 key
     * @return 返回 builder 对象
     */
    public static Builder create(String appId, String appKey) {
        return new Builder(appId, appKey);
    }

    @Getter
    @Setter
    public static class Builder {
        private Long expiredAt; // token 过期时间
        private String appId;   // 应用的 ID
        private String appKey;  // 应用的 key
        private TreeMap<String, String> params = new TreeMap<>(); // payload 的参数

        public Builder(String appId, String appKey) {
            Assert.notNull(appId, "JWT appId cannot be null");
            Assert.notNull(appKey, "JWT appKey cannot be null");
            this.appId = appId;
            this.appKey = appKey;
        }

        /**
         * 设置 token 的过期时间
         *
         * @param expiredAt 过期时间，单位是毫秒
         * @return 返回 builder 自己
         */
        public Builder expiredAt(long expiredAt) {
            this.expiredAt = expiredAt;
            return this;
        }

        /**
         * 添加用户数据到 token 中
         *
         * @param name  数据的 key
         * @param value 数据的 value
         * @return 返回 builder 自己
         */
        public Builder param(String name, String value) {
            Assert.notNull(name,  "JWT param name cannot be null");
            Assert.notNull(value, "JWT param value cannot be null");
            params.put(name, value);

            return this;
        }

        /**
         * 使用 appId, appKey, signedAt [, expiredAt], params 生成 token.
         * 生成算法为:
         *     1. 添加 appId, signedAt[, 如果 expiredAt 不为 null 也加入] 到 params 中
         *     2. 初始化计算签名的字符串 signedText 为 appKey
         *     3. 按照 params 中 key 的字母序遍历 params，value 挨个的加在到 signedText 后面
         *     4. signature = MD5(signedText)
         *     5. 把 params 转换为 JSON 字符串并使用 URL Safe 的 BASE64 对其进行编码得到 payload
         *     6. 最后得到的签名结果为 payload.signature
         *
         * @return 返回使用 JWT 签名的字符串
         */
        public String token() {
            // 添加签名需要的数据项
            params.put("appId", appId);
            params.put("signedAt", System.currentTimeMillis()+"");

            if (expiredAt != null) {
                params.put("expiredAt", expiredAt+"");
            }

            // 计算签名
            String payload   = Utils.base64UrlSafe(JSON.toJSONString(params));
            String signature = Jwt.sign(params, appKey);
            return payload + "." + signature;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String appId  = "school-1";
        String appKey = "Passw0rd";
        String token;

        // 1. 没有期限的 token, 一直有效
        token = Jwt.create(appId, appKey).param("username", "放下").token();
        System.out.println(token);
        System.out.println(Jwt.checkToken(token, appKey));
        System.out.println(StringUtils.repeat("-", 120));

        // 2. 有效期为 2 秒，2 秒后过期
        token = Jwt.create(appId, appKey).param("username", "放下").expiredAt(System.currentTimeMillis() + 2000).token();
        System.out.println(token);
        System.out.println(Jwt.checkToken(token, appKey));
        Thread.sleep(2500);
        System.out.println(Jwt.checkToken(token, appKey));

        System.out.println(Jwt.params(token));
        System.out.println(StringUtils.repeat("-", 80));

        // 3. 乱给的 token, 无效
        System.out.println(Jwt.checkToken("", appKey));
        System.out.println(Jwt.checkToken("xxx", appKey));
        System.out.println(Jwt.checkToken("xxx.yyy", appKey));
    }
}
