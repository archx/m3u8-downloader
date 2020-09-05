package com.github.archx.m3u8d.uitl;

import com.github.archx.m3u8d.entity.TaskMetaEntity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

/**
 * NetUtils
 *
 * @author archx
 * @since 2020/9/4 23:51
 */
@Slf4j
public final class NetUtils {

    public static String getRootDirectory() {
        URL resource = NetUtils.class.getClassLoader().getResource("");
        if (resource != null) {
            String file = resource.getFile();
            return new File(file).getPath();
        } else {
            return FileSystemView.getFileSystemView().getHomeDirectory().getPath();
        }
    }

    public static Optional<TaskMetaEntity> getTaskMeta(String path, String url) {

        TaskMetaEntity task = new TaskMetaEntity();
        task.setPath(path);
        task.setUrl(url);

        String prefix = url.substring(0, url.lastIndexOf("/"));

        Call call = new OkHttpClient().newCall(new Request.Builder().url(url).build());

        try (Response response = call.execute()) {
            if (response.isSuccessful()) {
                Set<String> names = response.headers().names();
                if (names.contains("Content-Range") || names.contains("Accept-Ranges")) {
                    task.setCanResume(true);
                }

                ResponseBody body = response.body();
                if (body != null) {
                    BufferedSource source = body.source();
                    String line = null;
                    while ((line = source.readUtf8Line()) != null) {
                        if (line.endsWith(".ts") || line.endsWith(".TS")) {
                            task.add(prefix + "/" + line);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            log.error("解析任务失败 err: ", ex);
            return Optional.empty();
        }

        return Optional.of(task);
    }
}
