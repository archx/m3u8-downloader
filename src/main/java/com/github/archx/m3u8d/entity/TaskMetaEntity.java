package com.github.archx.m3u8d.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskMetaEntity
 *
 * @author archx
 * @since 2020/9/4 23:34
 */
@Data
public class TaskMetaEntity {

    /**
     * 存储路径
     */
    private String path;

    /**
     * 链接地址
     */
    private String url;

    /**
     * 是否支持断点续传
     */
    private boolean canResume;

    /**
     * TS 分片
     */
    private List<String> fragments;

    public void add(String fragment) {
        if (fragments == null) {
            fragments = new ArrayList<>();
        }
        fragments.add(fragment);
    }
}
