package com.imokkkk.server.processor;

import com.imokkkk.model.Message;

/**
 * @author liuwy
 * @date 2023-05-11 10:49
 * @since 1.0
 */
public interface TaskProcessor {

    Runnable execAsyncTask(Message msg);
}
