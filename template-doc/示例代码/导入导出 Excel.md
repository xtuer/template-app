导入导出 Excel 使用 **EasyPoi**，下面就举个简单的例子，导出学员的信息，更多的如使用 Excel 模版导入导出，复杂效果，导出时使用运算等请参考 [EasyPoi教程](http://www.afterturn.cn/doc/easypoi.html)。

## 定义类 Student

要导出的属性使用注解 `@Excel`

```java
import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class Student {
    private long id; // 不导出

    @Excel(name = "学生姓名")
    private String name;

    @Excel(name = "性别", replace = {"男_1", "女_2"}, suffix = "生")
    private int gender;

    @Excel(name = "出生日期", format = "yyyy-MM-dd")
    private Date birthday;

    @Excel(name = "入学日期", format = "yyyy-MM-dd")
    private Date registrationDate;
}
```

> 比较有意思的是 `replace`，格式可以理解为 `ExcelValue_JavaValue`: **JavaValue** 为代码中的值，**ExcelValue** 为 Excel 文件中的值:
>
> * 导出时 **JavaValue** 会被替换为 **ExcelValue** 保存到 Excel 文件中
> * 导入时 Excel 文件里的 **ExcelValue** 会被替换为 **JavaValue** 来设置代码中的值

## 执行导出

使用 `ExcelExportUtil.exportExcel()` 执行导出

```java
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        List<Student> students = new LinkedList<>();
        students.add(new Student().setId(1).setName("无名").setGender(1).setBirthday(new Date()));
        students.add(new Student().setId(2).setName("杜非").setGender(1).setBirthday(new Date()));
        students.add(new Student().setId(3).setName("小白").setGender(2).setBirthday(new Date()));
        
        Workbook book = ExcelExportUtil.exportExcel(new ExportParams("计算机一班学生", "学生"), Student.class, students);
        book.write(new FileOutputStream("/Users/Biao/Desktop/x.xls"));
    }
}
```

> 导出的 excel 在数据行最上面有个标题 **计算机一班学生**，如果不需要，则 ExportParams 的第一个参数设置为 null:
>
> `ExcelExportUtil.exportExcel(new ExportParams(null, "学生"), Student.class, students)`

## 导出结果

| 计算机一班学生 |      |            |      |
| ------- | ---- | ---------- | ---- |
| 学生姓名    | 性别   | 出生日期       | 入学日期 |
| 无名      | 男生   | 2018-01-06 |      |
| 杜非      | 男生   | 2018-01-06 |      |
| 小白      | 女生   | 2018-01-06 |      |

## 执行导入

使用 `ExcelExportUtil.importExcel()` 执行导出，导入上面导出得到的 Excel

```java
package poi;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        ImportParams params = new ImportParams();
        // params.setTitleRows(1); // 默认为 0，大多数时候可省略 (注意这里，此处用 1 是因为第一行是 title，不是列名)
        // params.setHeadRows(1);  // 默认为 1，大多数时候可省略
        // params.setImportFields(new String[]{"姓名", "性别"}); // Excel 中必须有这几个列，否则格式不正确抛异常
        List<Student> students = ExcelImportUtil.importExcel(new File("/Users/Biao/Desktop/x.xls"), Student.class, params);
        System.out.println(JSON.toJSONString(students));
    }
}
```

> `titleRows`: 一般导入的 Excel 第一行就是列名，此时 titleRows 使用 0，上面的程序使用 1 是因为导入的文件的第一行增加了一个标题，第二行才是列名。

