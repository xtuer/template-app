package ebag.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * 常用功能的工具类，例如计算 MD5, Base64，UUID 等
 */
public final class Utils {
    private static PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    /**
     * BindingResult 中的错误信息很多，对用户不够友好，使用 getBindingMessage()
     * 提取对用户阅读友好的定义验证规则 message.
     *
     * @param result 验证的结果对象
     * @return 验证规则 message
     */
    public static String getBindingMessage(BindingResult result) {
        StringBuffer sb = new StringBuffer();

        for (FieldError error : result.getFieldErrors()) {
            // sb.append(error.getField() + " : " + error.getDefaultMessage() + "\n");
            sb.append(error.getDefaultMessage() + "\n");
        }

        return sb.toString();
    }

    /**
     * 计算字符串的 MD5.
     *
     * @param text 需要计算 MD5 的字符串
     * @return 返回字符串的 MD5
     */
    public static String md5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes());
    }

    /**
     * 计算文件的 MD5.
     * MD5 包含 16 进制表示的 10 个字符: 0-9, a-z
     *
     * @param file 需要计算 MD5 的文件
     * @return 返回文件的 MD5，如果出错，例如文件不存在则返回 null
     */
    public static String md5(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 对字符串 text 进行 Base64 编码.
     * Base64 有 64 个字符: 0-9, a-z, A-Z, +, /
     * 等号 = 用于补齐.
     *
     * @param text 要进行编码的字符串
     * @return 返回使用 Base64 编码后的字符串
     */
    public static String base64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    /**
     * 解码 Base64 编码的字符串 base64Text.
     *
     * @param base64Text Base64 编码的字符串
     * @return 返回源字符串
     */
    public static String unbase64(String base64Text) {
        return new String(Base64.getDecoder().decode(base64Text));
    }

    /**
     * 对字符串 text 进行 URL Safe 的 Base64 编码: +, /, = 被置换 为 -, _, *，只包含 65 个 URL safe 的字符: 0-9, a-z, A-Z, -, _, *
     * 注意: Base64.getUrlEncoder() 编码后还有 =，不能使用
     *
     * 系统中有一些值使用 BASE64 编码后存储在 COOKIE 中, 当编码后的字符串最后有一个或者两个等号(=)时,
     * 使用 Request.getCookies().getValue() 会丢失等号, 再用 BASE64 解码时产生错误.
     *
     * @param text 要进行编码的字符串
     * @return 返回使用 URL Safe Base64 编码后的字符串
     */
    public static String base64UrlSafe(String text) {
        String base64Text = Utils.base64(text);
        base64Text = base64Text.replace('+', '-');
        base64Text = base64Text.replace('/', '_');
        base64Text = base64Text.replace('=', '*');

        return base64Text;
    }

    /**
     * 解码 URL Safe 的 Base64 编码的字符串 urlBase64Text.
     *
     * @param urlBase64Text URL Safe 的 Base64 编码的字符串
     * @return 返回源字符串
     */
    public static String unbase64UrlSafe(String urlBase64Text) {
        urlBase64Text = urlBase64Text.replace('-', '+');
        urlBase64Text = urlBase64Text.replace('_', '/');
        urlBase64Text = urlBase64Text.replace('*', '=');

        return Utils.unbase64(urlBase64Text);
    }

    /**
     * 生成 UUID，格式为 1E87E000-92C0-4660-B00D-FF92B37B0A7B
     *
     * @return 返回 UUID
     */
    public static String uuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    /**
     * 使用 BCrypt 算法对密码进行加密
     *
     * @param rawPassword 原始密码
     * @return 返回 BCrypt 加密的密码 (带前缀 {bcrypt})
     */
    public static String passwordByBCrypt(String rawPassword) {
        return "{bcrypt}" + bCryptPasswordEncoder.encode(rawPassword);
    }

    /**
     * 使用 BCrypt 算法判断密码是否有效
     *
     * @param rawPassword       原始密码
     * @param encryptedPassword 加密后的密码
     * @return 密码匹配返回 true，否则返回 false
     */
    public static boolean isPasswordValidByBCrypt(String rawPassword, String encryptedPassword) {
        encryptedPassword = encryptedPassword.replace("{bcrypt}", ""); // 去除前缀
        return bCryptPasswordEncoder.matches(rawPassword, encryptedPassword);
    }

    public static void main(String[] args) {
        String text = "如果要编码的字节数不能被3整除，最后会多出1个或2个字节.";
        String encrypt    = base64(text);
        String encryptUrl = base64UrlSafe(text);
        System.out.println(encrypt);
        System.out.println(encryptUrl);
        System.out.println(unbase64(encrypt));
        System.out.println(unbase64UrlSafe(encryptUrl));

        // 测试密码
        System.out.println(passwordByBCrypt("admin"));
        System.out.println(isPasswordValidByBCrypt("password", "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"));
        System.out.println(isPasswordValidByBCrypt("password", "{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"));
    }
}

