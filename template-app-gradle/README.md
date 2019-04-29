## 开发环境

* 运行类 Foo: `gradle clean run -DmainClass=Foo`

  > 为什么不在 IDE 中运行呢？因为未打包前配置在 config.groovy 中，直接从 IDEA 中运行 main 函数不会把配置自动替换到 application.properties 文件里，有可能因为找不到配置而运行失败

* 运行类 Foo: 也可以先 `gradle build` 把生成的配置文件复制到 IDEA 需要的位置 (默认在 out 文件夹下)，然后从 IDEA 里运行

* 打包: `gradle -Denv=production clean shadowJar`，生成 `build/libs/impex.zip`

## 线上环境

运行 `java -jar -Dfile.encoding=UTF-8 impex.jar` 或者解压，运行 `java -Dfile.encoding=UTF-8 Foo`

> -Dfile.encoding=UTF-8 指定运行时的编码为 UTF-8，避免使用系统默认编码(Windows 为 GB2312，Mac 为 UTF-8)

