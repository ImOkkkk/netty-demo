package com.imokkkk.server.processor;

import io.netty.util.NettyRuntime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-11 10:46
 * @since 1.0
 */
@Slf4j
public class AsyncBizProcessor {
    private static BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<Runnable>(3000);
    private static ExecutorService executorService =
            new ThreadPoolExecutor(
                    1, NettyRuntime.availableProcessors(), 60, TimeUnit.SECONDS, taskQueue);

    public static void submitTask(Runnable task) {
        executorService.execute(task);
    }
}
