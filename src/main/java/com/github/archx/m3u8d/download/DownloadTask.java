package com.github.archx.m3u8d.download;

import com.github.archx.m3u8d.download.body.ProgressResponseBody;
import com.github.archx.m3u8d.download.listener.DownloadListener;
import com.github.archx.m3u8d.download.listener.ProgressListener;
import com.github.archx.m3u8d.entity.TaskMetaEntity;
import com.github.archx.m3u8d.entity.TsFileEntity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DownloadTask
 *
 * @author archx
 * @since 2020/9/5 8:53
 */
@Slf4j
public class DownloadTask {

    private final ExecutorService executorService;
    private final TaskMetaEntity taskMeta;
    private final DownloadListener downloadListener;

    private List<TsFileEntity> tsFileList = new ArrayList<>();

    public DownloadTask(ExecutorService executorService, TaskMetaEntity taskMeta, DownloadListener downloadListener) {
        this.executorService = executorService;
        this.taskMeta = taskMeta;
        this.downloadListener = downloadListener;

        this.initData();
    }

    public List<TsFileEntity> getTsFileList() {
        return tsFileList;
    }

    public void pause() {
        // TODO
        throw new UnsupportedOperationException("not implementation");
    }

    public void start() {
        final int ud = tsFileList.size();
        executorService.execute(() -> {
            final AtomicInteger num = new AtomicInteger(0);
            int index = 0;
            try {
                while (index < ud) {
                    // 最多5个线程同时下载一个任务
                    if (num.get() < 5) {
                        num.incrementAndGet();
                        TsFileEntity ts = tsFileList.get(index++);
                        executorService.execute(() -> {
                            download(ts);
                            num.decrementAndGet();
                        });
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException ex) {
                log.error("下载出错", ex);
            }
        });
    }

    private void download(TsFileEntity ts) {

        Request.Builder rb = new Request.Builder().url(ts.getUrl());
        // if (taskMeta.isCanResume()) {
        // TODO
        // rb.header("RANGE", "bytes=" + 0 + "-"); // 断点续传
        // }
        Request request = rb.build();

        // 进度监听
        ProgressListener progress = (n, u, value) -> {
            downloadListener.processing(ts.getName(), ts.getUrl(), value);
        };

        // 拦截器
        Interceptor interceptor = chain -> {
            Response proceed = chain.proceed(chain.request());
            ProgressResponseBody body = new ProgressResponseBody(proceed, ts.getStartPos(), progress);
            return proceed.newBuilder().body(body).build();
        };

        Thread currentThread = Thread.currentThread();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(interceptor).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                downloadListener.failed(ts.getName(), ts.getUrl(), e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                long length = response.body().contentLength();
                if (length == 0) {
                    // 说明文件已经下完成
                    downloadListener.completed(ts.getName(), ts.getUrl(), ts.getAbsolutePath());
                    return;
                }
                downloadListener.start(ts.getName(), ts.getUrl(), length + ts.getStartPos());

                byte[] buff = new byte[2048];
                int len;
                try (InputStream is = response.body().byteStream();
                     BufferedInputStream bis = new BufferedInputStream(is)) {
                    // 指定跳过位置 开始断点续传
                    // TODO
                    File file = new File(ts.getAbsolutePath());
                    long newPos = ts.getStartPos();
                    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd")) {
                        randomAccessFile.seek(ts.getStartPos());
                        while (!currentThread.isInterrupted() && (len = bis.read(buff)) != -1) {
                            randomAccessFile.write(buff, 0, len);
                            // 更新下载的位置
                            newPos += len;
                            ts.setStartPos(newPos); // 可以写入文件或其他位置
                        }
                        downloadListener.completed(ts.getName(), ts.getUrl(), ts.getAbsolutePath());
                    }
                } catch (IOException ex) {
                    downloadListener.failed(ts.getName(), ts.getUrl(), ex.getMessage());
                    throw ex;
                }
            }
        });
    }

    private void initData() {
        // 文件路径
        String path = taskMeta.getPath();
        // 创建目录
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            boolean flg = dir.mkdirs();
            if (!flg) {
                log.error("无法创建目录 path - '{}'", path);
                return;
            }
        }

        List<String> fragments = taskMeta.getFragments();
        for (String fragment : fragments) {
            TsFileEntity ts = new TsFileEntity();
            ts.setUrl(fragment);
            ts.setStartPos(0);
            ts.setProgress(0);
            ts.setSavePath(path);
            ts.setName(fragment.substring(fragment.lastIndexOf("/") + 1));
            tsFileList.add(ts);
        }
    }
}
