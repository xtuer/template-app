package com.xtuer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtuer.bean.Mime;
import com.xtuer.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 常用功能的工具类，例如计算 MD5, Base64，UUID 等
 */
@Slf4j
public final class Utils {
    private static final PasswordEncoder B_CRYPT_PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final DateFormat DATE_FORMATTER_WITH_WEEKDAY = new SimpleDateFormat("M 月 d 日 E", Locale.SIMPLIFIED_CHINESE);

    /**
     * BindingResult 中的错误信息很多，对用户不够友好，使用 getBindingMessage()
     * 提取对用户阅读友好的定义验证规则 message.
     *
     * @param result 验证的结果对象
     * @return 验证规则 message
     */
    public static String getBindingMessage(BindingResult result) {
        StringBuilder sb = new StringBuilder();

        for (FieldError error : result.getFieldErrors()) {
            // sb.append(error.getField() + " : " + error.getDefaultMessage() + "\n");
            sb.append(error.getDefaultMessage()).append("\n");
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
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
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
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解码 Base64 编码的字符串 base64Text.
     *
     * @param base64Text Base64 编码的字符串
     * @return 返回源字符串
     */
    public static String unbase64(String base64Text) {
        return new String(Base64.getDecoder().decode(base64Text), StandardCharsets.UTF_8);
    }

    /**
     * 对字符串 text 进行 URL Safe 的 Base64 编码: +, /, =, 被置换为 -, _, *, 只包含 64 个 URL safe 的字符: 0-9, a-z, A-Z, -, _
     * 注意: Base64.getUrlEncoder() 编码后的 Base64 结果还有 =，不能使用
     * <p>
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
        return "{bcrypt}" + B_CRYPT_PASSWORD_ENCODER.encode(rawPassword);
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
        return B_CRYPT_PASSWORD_ENCODER.matches(rawPassword, encryptedPassword);
    }

    /**
     * 今天日期的字符串表示，格式为 yyyy-MM-dd
     *
     * @return 返回格式为 yyyy-MM-dd 日期字符串
     */
    public static String today() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
    }

    /**
     * 获取 date 这一天的开始时间，例如 2020-11-11 00:00:00
     *
     * @param date 日期
     * @return 返回 date 这一天的开始时间
     */
    public static Date startOfDay(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 获取 date 这一天的最后时间，例如 2020-11-11 23:59:59
     *
     * @param date 日期
     * @return 返回 date 这一天的最后时间
     */
    public static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    /**
     * 输出对象到控制台
     *
     * @param object 要输出的对象
     */
    public static void dump(Object object) {
        System.out.println(Utils.toJson(object));
    }

    /**
     * 把对象转为 Json 字符串
     *
     * @param object 要转为 Json 字符串的对象
     * @return 返回对象的 Json 字符串表示
     */
    public static String toJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Date format
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        // Indent
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // 2 个空格
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        printer.indentObjectsWith(indenter); // Indent JSON objects
        printer.indentArraysWith(indenter);  // Indent JSON arrays

        try {
            return objectMapper.writer(printer).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * 把 JSON 字符串转为对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类
     * @return 返回得到的对象，转换失败时返回 null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 把 JSON 字符串转为对象
     *
     * @param json JSON 字符串
     * @param ref  类型引用，用于集合类型
     * @return 返回得到的对象，转换失败时返回 null
     */
    public static <T> T fromJson(String json, TypeReference<T> ref) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, ref);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 格式化日期为: M 月 d 日 星期几
     *
     * @param date 日期
     * @return 返回格式化后的日期字符串
     */
    public static String formatDateWithWeekDay(Date date) {
        if (date != null) {
            return DATE_FORMATTER_WITH_WEEKDAY.format(date);
        } else {
            return null;
        }
    }

    /**
     * 判断 ID 是否有效，有效 ID 从 1 开始
     *
     * @param id 进行有效性检查的 ID
     * @return id 大于 0 时返回 true，否则返回 false
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 判断 ID 是否无效，有效 ID 从 1 开始
     *
     * @param id 进行有效性检查的 ID
     * @return id 为 null 或者小于等于 0 返回 true，否则返回 false
     */
    public static boolean isInvalidId(Long id) {
        return !isValidId(id);
    }

    /**
     * 根据文件名 filename 判读它是否图片
     *
     * @param filename 文件名
     * @return 如果文件是图片则返回 true，否则返回 false
     */
    public static boolean isImage(String filename) {
        return Mime.isImage(filename);
    }

    /**
     * 获取图片的大小
     *
     * This solution is very quick as only image size is read from the file and not the whole image.
     * From: https://stackoverflow.com/questions/672916/how-to-get-image-height-and-width-using-java
     *
     * Blows ImageIO.read() completely out of the water, both in terms of CPU time and memory usage.
     *
     * @param path 图片文件的路径
     * @return 返回图片大小的 dimension 对象
     */
    public static Dimension getImageSize(String path) {
        Dimension result = null;
        String suffix = FilenameUtils.getExtension(path);
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);

        if (iter.hasNext()) {
            ImageReader reader = iter.next();

            try (ImageInputStream stream = new FileImageInputStream(new File(path))) {
                reader.setInput(stream);
                int width  = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                result = new Dimension(width, height);
            } catch (IOException e) {
                log.warn(e.getMessage());
            } finally {
                reader.dispose();
            }
        } else {
            log.warn("No ImageReader found for given format: " + suffix);
        }

        return result;
    }

    /**
     * 判断传入的文件是否普通文本文件
     *
     * @param filename 文件名
     * @return 如果是普通文本文件返回 true，否则返回 false
     */
    public static boolean isPlainTextFile(String filename) {
        return Mime.isPlainTextFile(filename);
    }

    /**
     * 浏览器中内嵌显示，不进行下载的文件
     *
     * @param filename 文件名
     * @return 如果是浏览器中内嵌显示的文件则返回 true，否则返回 false
     */
    public static boolean isInlineFileForBrowser(String filename) {
        return Mime.isInlineFile(filename);
    }

    /**
     * 使用从 classes 目录下相对路径为 path 的文件创建 InputStream
     *
     * @param path 文件相对 classes 目录的路径
     * @return 返回文件 path 对应的 InputStream
     */
    public static InputStream getStreamRelativeToClassesDirectory(String path) {
        return Utils.class.getClassLoader().getResourceAsStream(path);
    }

    /**
     * 把 List<T> 根据 key 进行分组为 Map<K, List<T>>，key 为类 T 的方法引用返回的值，例如用户名，ID 等。
     * 使用案例，把 users 根据用户名和 ID 进行分组，相同用户名的用户作为 map 的 value (List<User>)，并且限制每个 list 大小为 2:
     *     List<User> users = new LinkedList<>();
     *     Map<String, List<User>> usersMap1 = groupAndLimitMapListValueSize(users, 2, User::getUsername);
     *     Map<Long,   List<User>> usersMap2 = groupAndLimitMapListValueSize(users, 2, User::getId);
     *
     * @param list 要进行分组的 list
     * @param size 每组元素的个数
     * @param classifier 分组的 key 的方法引用
     * @param <K> map 的 key 的类型
     * @param <T> map 的 value 的类型
     * @return 返回分组后的 map
     */
    public static <K, T> Map<K, List<T>> groupAndLimitMapListValueSize(List<T> list, int size, Function<? super T, ? extends K> classifier) {
        Map<K, List<T>> map = list.stream().collect(Collectors.groupingBy(classifier));

        map.forEach((key, valueList) -> {
            if (valueList.size() > size) {
                map.put(key, valueList.subList(0, size));
            }
        });

        return map;
    }

    /**
     * 使用 refW 和 refH 作为参考值, 对 w 和 h 进行等比缩放:
     *     Utils.scaleKeepAspectRatio(1000, 200, 1280, 720));  // 输出 [1000, 200]: 宽高都小于, 不进行缩放
     *     Utils.scaleKeepAspectRatio(1000, 2000, 1280, 720)); // 输出 [360,  720]: 高的比例大, 高作为参考缩放
     *     Utils.scaleKeepAspectRatio(1440, 300, 1280, 720));  // 输出 [1280, 266]: 宽的比例大, 宽作为参考缩放
     *     Utils.scaleKeepAspectRatio(2560, 1440, 1280, 720)); // 输出 [1280, 720]: 宽高比例一样, 任意一个作为参考缩放
     *
     * @param w    进行缩放的宽
     * @param h    进行缩放的高
     * @param refW 缩放参考的宽
     * @param refH 缩放参考的高
     * @return 返回缩放后的尺寸
     */
    public static Dimension scaleKeepAspectRatio(int w, int h, int refW, int refH) {
        // 1. 计算宽和高的比例
        // 2. 如果宽或高任意一个大于对应的参考宽或高, 则需要进行缩放
        // 3. 使用等比缩放, 取宽高比例中最大的比例作为缩放比例

        int resultW   = w;
        int resultH   = h;
        double ratioW = ((double) w) / refW; // 宽的比例
        double ratioH = ((double) h) / refH; // 高的比例
        double ratio  = ratioW > ratioH ? ratioW : ratioH; // 缩放比例

        if (w > refW || h > refH) {
            resultW = (int) (w / ratio);
            resultH = (int) (h / ratio);
        }

        return new Dimension(resultW, resultH);
    }

    /**
     * 读取服务器 ID 的环境变量 SERVER_ID，范围是 [0, 1023]，集群中每个服务器的 ID 唯一
     *
     * @return 返回服务器 ID，如果没有设置环境变量 SERVER_ID 则抛出异常
     */
    public static int getServerId() {
        String id = System.getenv("SERVER_ID");

        // 是整数则为有效的 ID
        if (NumberUtils.isDigits(id)) {
            return NumberUtils.toInt(id);
        } else {
            return -1;
            // throw new RuntimeException("请设置正确的环境变量 SERVER_ID，范围是 [0, 1023]");
        }
    }

    /**
     * 获取 resources 目录下的文件，如获取文件 resources/meta/x.pdf，则参数 path 为 meta/x.pdf
     *
     * @param path 相对于 resources 目录的路径
     * @return 返回找到的文件
     * @throws IOException 文件异常
     */
    public static File getResourceFile(String path) throws IOException {
        return new File(new ClassPathResource(path).getURI());
    }

    private static final String[] BASE_CN_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    /**
     * 阿拉伯数字转为中文数字，支持 [0, 99]
     *
     * @param n 阿拉伯数字
     * @return 返回 [0, 99] 之间的中文数字字符串，超出范围的返回 ''
     */
    public static String toCnNumber(int n) {
        if (0 <= n && n <= 10) {
            // 0-10: 零、一、...、十
            return BASE_CN_NUMBERS[n];
        } else if (11 <= n && n <= 99) {
            int ge  = n % 10;
            int shi = n / 10;

            if (1 == shi) {
                // 11-19: 十一、十二、...
                return "十" + (ge==0 ? "" : BASE_CN_NUMBERS[ge]);
            } else {
                // 20-99: 二十一、二十二、...
                return BASE_CN_NUMBERS[shi] + "十" + (ge==0 ? "" : BASE_CN_NUMBERS[ge]);
            }
        } else {
            log.info("数字 {} 越界，只支持 [0, 99] 之间的数字", n);
            return "";
        }
    };

    /**
     * 获取状态 Label
     *
     * @param statusLabels 状态的 Label 数组
     * @param state        状态值
     * @return 返回状态对应的 Label
     */
    public static String getStateLabel(String[] statusLabels, int state) {
        if (state >= 0 && state < statusLabels.length) {
            return statusLabels[state];
        } else {
            return "未知";
        }
    }

    /**
     * 花括号的 pattern
     */
    private static final Pattern PATTERN_BRACE = Pattern.compile("\\{}");

    /**
     * 使用数组 args 中的元素按顺序替换 text 中的 {} 占位符，如果 args 的元素个数少于 {} 的个数，则对应位置仍然返回 {}。
     *
     * 示例:
     * text = "用户{}的{}成绩不存在"
     * RegExpTest.replaceBracePlaceholder(text, "小明") => 用户小明的{}成绩不存在
     * RegExpTest.replaceBracePlaceholder(text, "小明", "语文") => 用户小明的语文成绩不存在
     *
     * @param text 要替换的字符串
     * @param args 替换 {} 的数组
     * @return 返回替换后的字符串
     */
    public static String replaceBracePlaceholder(String text, String ...args) {
        if (args.length == 0) {
            return text;
        }

        int[] index = new int[1]; // 为了个 Lambda 里传递可变的 int 数据

        return Utils.replace(text, PATTERN_BRACE, matcher -> {
            int i = index[0]++;

            if (i >= args.length) {
                return "{}";
            } else {
                return args[i];
            }
        });
    }

    /**
     * 替换字符串 text 中匹配 pattern 的子串，每个匹配的内容使用 converter 方法进行转换.
     *
     * @param text      要替换的字符串
     * @param pattern   正则表达式的 pattern
     * @param converter 转换函数
     * @return 返回替换后的字符串
     */
    public static String replace(String text, Pattern pattern, Function<Matcher, String> converter) {
        StringBuilder output = new StringBuilder();
        int indexAfterMatched = 0;
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // [indexAfterMatched, matcher.start()) 之间的内容为字符串中不匹配的内容，原样复制到结果串中
            output.append(text, indexAfterMatched, matcher.start()).append(converter.apply(matcher));
            indexAfterMatched = matcher.end();
        }

        output.append(text, indexAfterMatched, text.length());

        return output.toString();
    }

    public static void main(String[] args) {
        String text = "如果要编码的字节数不能被3整除，最后会多出1个或2个字节.";
        String encrypt = base64(text);
        String encryptUrl = base64UrlSafe(text);
        System.out.println(encrypt);
        System.out.println(encryptUrl);
        System.out.println(unbase64(encrypt));
        System.out.println(unbase64UrlSafe(encryptUrl));

        // 测试密码
        System.out.println(passwordByBCrypt("admin"));
        System.out.println(isPasswordValidByBCrypt("password", "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"));
        System.out.println(isPasswordValidByBCrypt("password", "{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"));

        // JSON Test
        System.out.println("--------------------------------- JSON ---------------------------------");
        User user = new User().setUserId(1L).setUsername("Alice");
        String json = Utils.toJson(user);
        System.out.println(json);
        user = Utils.fromJson(json, User.class);
        System.out.println(user);

        List<Integer> ns = Arrays.asList(1, 2, 3, 4, 5);
        json = Utils.toJson(ns);
        System.out.println(json);
        ns = Utils.fromJson(json, new TypeReference<List<Integer>> () {});
        System.out.println(ns);
    }
}
