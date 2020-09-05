package com.github.archx.m3u8d.entity;

import lombok.Data;

/**
 * TsFileEntity
 *
 * @author archx
 * @since 2020/9/5 9:38
 */
@Data
public class TsFileEntity {

    /**
     * 下载地址
     */
    private String url;

    /**
     * 文件名
     */
    private String name;

    /**
     * 开始下载位置
     */
    private long startPos = 0;

    /**
     * 进度占比 %
     */
    private int progress = 0;

    /**
     * 存储路径
     */
    private String savePath;

    /**
     * 状态 0: 未开始, 1: 进行中， 2: 已完成， -1: 已失败
     */
    private int status = 0;

    public String getAbsolutePath() {
        return savePath + "/" + name;
    }
}
