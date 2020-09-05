package com.github.archx.m3u8d.download.listener;

/**
 * DownloadListener
 *
 * @author archx
 * @since 2020/9/5 9:45
 */
public interface DownloadListener extends ProgressListener {

    void start(String name, String url, long maxLength);

    void completed(String name, String url, String path);

    void failed(String name, String url, String message);
}
