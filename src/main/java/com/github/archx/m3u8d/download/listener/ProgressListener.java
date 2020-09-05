package com.github.archx.m3u8d.download.listener;

/**
 * ProgressListener
 *
 * @author archx
 * @since 2020/9/5 8:55
 */
public interface ProgressListener {

    /**
     * 进度
     *
     * @param name     文件名
     * @param url      下载地址
     * @param progress 百分比
     */
    void processing(String name, String url, int progress);
}
