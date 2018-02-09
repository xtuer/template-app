package com.xtuer.mapper;

import com.xtuer.bean.Demo;

import java.util.List;

public interface DemoMapper {
    Demo findDemoById(int id);
    List<Demo> allDemos();
}
