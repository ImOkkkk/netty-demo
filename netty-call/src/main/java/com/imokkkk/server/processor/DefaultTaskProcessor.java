package com.imokkkk.server.processor;

import com.imokkkk.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuwy
 * @date 2023-05-11 10:49
 * @since 1.0
 */
public class DefaultTaskProcessor implements TaskProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskProcessor.class);

    @Override
    public Runnable execAsyncTask(Message msg) {
        Runnable task =
                new Runnable() {
                    @Override
                    public void run() {
                        LOG.info("DefaultTaskProcessor模拟任务处理：" + msg.getBody());
                    }
                };
        return task;
    }
}
