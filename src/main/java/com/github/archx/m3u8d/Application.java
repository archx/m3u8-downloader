package com.github.archx.m3u8d;

import com.github.archx.m3u8d.view.MainWindow;
import lombok.val;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Application
 *
 * @author archx
 * @since 2020/9/4 21:44
 */
public class Application {

    public static void main(String[] args) {
        val win = new MainWindow("M3U8 Downloader");
        // 初始化线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3,
                10,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("OkHttp");
                    thread.setDaemon(false);

                    System.out.println("thread created " + thread.getId());
                    return thread;
                });
        win.setExecutorService(executor);
        win.startup();
    }
}
