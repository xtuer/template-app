package com.xtuer.bean;

import com.xtuer.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 文件信息类, 用于判断文件是否图片, 视频, 音频等, 是否可转换为 PDF 或者 MP4, 获取文件对应的 HTTP content type.
 */
@Slf4j
final public class Mime {
    // 图片的后缀
    private static final String[] IMAGE_FILE_EXTENSIONS = {
            "png", "svg", "ico", "tif", "bmp", "jpg", "jpeg", "gif"
    };

    // 视频的后缀
    private static final String[] VIDEOS_FILE_EXTENSIONS = {
            "mp4", "avi", "flv", "swf", "wmv", "mov", "3gp", "mpg", "rmvb", "mkv"
    };

    // 音频的后缀
    private static final String[] AUDIOS_FILE_EXTENSIONS = {
            "mp3"
    };

    // 普通文本文件的后缀
    private static final String[] PLAIN_TEXT_FILE_EXTENSIONS = {
            "txt", "c", "h", "cpp", "java", "py", "sh", "bat", "html", "js", "css", "md", "json", "xml"
    };

    // 浏览器中内嵌显示，不进行下载的文件名后缀
    private static final String[] INLINE_FILE_EXTENSIONS = {
            "pdf",
            "png", "svg", "ico", "tif", "bmp", "jpg", "jpeg",
            "txt", "c", "h", "cpp", "java", "py", "sh", "bat", "html", "js", "css", "md", "json", "xml"
    };

    // 可转换为 PDF 文件的后缀名
    private static final String[] CAN_TO_PDF_FILE_EXTENSIONS = {
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    };

    // Content type 的 properties
    private static final Properties CONTENT_TYPE_PROPS = new Properties();

    static {
        // 从文件 static/meta/content-type.properties 中加载 content type
        try (InputStream in = Utils.getStreamRelativeToClassesDirectory("static/meta/content-type.properties")) {
            CONTENT_TYPE_PROPS.load(in);
            log.info("[成功] 加载 mime 文件: static/meta/content-type.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件名 filename 获取它的 content type
     *
     * @param filename 文件名
     * @return 返回文件对应的 content type
     */
    public static String getContentType(String filename) {
        String extension   = FilenameUtils.getExtension(filename).toLowerCase();
        String contentType = CONTENT_TYPE_PROPS.getProperty("." + extension);
        return StringUtils.isNotBlank(contentType) ? contentType : "application/octet-stream";
    }

    /**
     * 根据文件名 filename 判读它是否图片
     *
     * @param filename 文件名
     * @return 如果文件是图片则返回 true，否则返回 false
     */
    public static boolean isImage(String filename) {
        return fileExtensionIn(filename, IMAGE_FILE_EXTENSIONS);
    }

    /**
     * 根据文件名 filename 判读它是否视频
     *
     * @param filename 文件名
     * @return 如果文件是视频则返回 true，否则返回 false
     */
    public static boolean isVideo(String filename) {
        return fileExtensionIn(filename, VIDEOS_FILE_EXTENSIONS);
    }

    /**
     * 根据文件名 filename 判读它是否音频
     *
     * @param filename 文件名
     * @return 如果文件是音频则返回 true，否则返回 false
     */
    public static boolean isAudio(String filename) {
        return fileExtensionIn(filename, AUDIOS_FILE_EXTENSIONS);
    }

    /**
     * 浏览器中内嵌显示，不进行下载的文件
     *
     * @param filename 文件名
     * @return 如果是浏览器中内嵌显示的文件则返回 true，否则返回 false
     */
    public static boolean isInlineFile(String filename) {
        return fileExtensionIn(filename, INLINE_FILE_EXTENSIONS);
    }

    /**
     * 判断传入的文件是否普通文本文件
     *
     * @param filename 文件名
     * @return 如果是普通文本文件返回 true，否则返回 false
     */
    public static boolean isPlainTextFile(String filename) {
        return fileExtensionIn(filename, PLAIN_TEXT_FILE_EXTENSIONS);
    }

    /**
     * 判断传入的文件是否 PDF
     *
     * @param filename 文件名
     * @return 是 PDF 返回 true, 否则返回 false
     */
    public static boolean isPdf(String filename) {
        return fileExtensionIn(filename, new String[] { "pdf" });
    }

    /**
     * 判断传入的文件是否 MP4
     *
     * @param filename 文件名
     * @return 是 MP4 返回 true, 否则返回 false
     */
    public static boolean isMp4(String filename) {
        return fileExtensionIn(filename, new String[] { "mp4" });
    }

    /**
     * 判断文件是否可转为 PDF
     *
     * @param filename 文件名
     * @return 如果文件可转换为 PDF 则返回 true, 否则返回 false
     */
    public static boolean canConvertToPdf(String filename) {
        return fileExtensionIn(filename, CAN_TO_PDF_FILE_EXTENSIONS);
    }

    /**
     * 判断文件是否可转换为 MP4
     *
     * @param filename 文件名
     * @return 如果文件可转换为 MP4 则返回 true, 否则返回 false
     */
    public static boolean canConvertToMp4(String filename) {
        return fileExtensionIn(filename, VIDEOS_FILE_EXTENSIONS);
    }

    /**
     * 判断文件名后缀是否在数组 extensions 中, 忽略大小写
     *
     * @param filename   文件名
     * @param extensions 后缀的数组
     * @return 如果文件名的后缀在数组 extensions 中则返回 true，否则返回 false
     */
    private static boolean fileExtensionIn(String filename, String[] extensions) {
        if (StringUtils.isBlank(filename)) {
            return false;
        }

        String fileExt = FilenameUtils.getExtension(filename).toLowerCase(); // 获取文件名后缀

        for (String ext : extensions) {
            // 如果文件名后缀在 extensions 中则返回 true
            if (ext.equals(fileExt)) {
                return true;
            }
        }

        return false;
    }
}
