package com.github.archx.m3u8d.view.handler;

import com.github.archx.m3u8d.entity.TaskMetaEntity;

/**
 * TaskHandler
 *
 * @author archx
 * @since 2020/9/4 23:33
 */
public interface TaskHandler {

    void handle(TaskMetaEntity task);
}
