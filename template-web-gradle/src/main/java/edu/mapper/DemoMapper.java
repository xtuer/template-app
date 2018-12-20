package edu.mapper;

import edu.bean.Demo;

import java.util.List;

public interface DemoMapper {
    Demo findDemoById(int demoId);
    List<Demo> findDemos();

    // Java 8 使用 -parameters 把参数名编译到 class 中，
    // 这样 MyBatis 传递多个参数时就不必使用 @Param 了
    Demo findDemoByIdAndInfo(int demoId, String info);
}
