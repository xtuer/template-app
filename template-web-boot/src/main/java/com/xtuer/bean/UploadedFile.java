package com.xtuer.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 上传的文件的对象
 */
@Getter
@Setter
@Accessors(chain = true)
public class UploadedFile {
    public static final int TEMPORARY_FILE = 0;
    public static final int PLATFORM_FILE  = 1;
    public static final int TEACHER_FILE   = 2;
    public static final int STUDENT_FILE   = 3;

    private long   id;          // 每个上传的文件都有一个唯一的 ID
    private String filename;    // 文件的原始名字
    private String url;         // 访问文件的 URL
    private int    type;        // 文件的类型: 0 (临时文件), 1 (系统管理员上传的文件), 2 (老师上传的文件), 3 (学生上传的文件)
    private long   userId;      // 上传文件的用户的 ID
    private int    imageWidth;  // 上传图片的宽度
    private int    imageHeight; // 上传图片的宽度

    public UploadedFile() {

    }

    public UploadedFile(long id, String filename, String url, int type, long userId) {
        this.id = id;
        this.filename = filename;
        this.url = url;
        this.type = type;
        this.userId = userId;
    }
}
