package com.imokkkk.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;

import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liuwy
 * @date 2023-05-09 16:51
 * @since 1.0
 */
@Sharable
@Slf4j
public class MetricsHandler extends ChannelDuplexHandler {
    private static AtomicBoolean startTask = new AtomicBoolean(false);
    private static AtomicLong chCount = new AtomicLong(0);
    private static AtomicLong totalReadBytes = new AtomicLong(0);
    private static AtomicLong totalWriteBytes = new AtomicLong(0);
    private static ScheduledExecutorService statService = new ScheduledThreadPoolExecutor(1);
    // 保存所有已连接的对话
    private static final ChannelGroup channelGroup =
            new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        chCount.incrementAndGet();
        if (startTask.compareAndSet(false, true)) {
            statService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            log.info("======性能指标采集开始======");
                            log.info("当前在线数量：{}", chCount.get());
                            // IO线程池待处理队列大小
                            Iterator<EventExecutor> executorGroups =
                                    ctx.executor().parent().iterator();
                            while (executorGroups.hasNext()) {
                                SingleThreadEventExecutor executor =
                                        (SingleThreadEventExecutor) executorGroups.next();
                                int size = executor.pendingTasks();
                                if (executor == ctx.executor()) {
                                    log.info(
                                            "【{}】:【{}】待处理队列大小：【{}】", ctx.channel(), executor, size);
                                } else {
                                    log.info("【{}】待处理队列大小：【{}】", executor, size);
                                }
                            }
                            // 发送队列积压字节数
                            Iterator<Channel> channels = channelGroup.iterator();
                            while (channels.hasNext()) {
                                Channel channel = channels.next();
                                if (channel instanceof ServerChannel) {
                                    continue;
                                }
                                log.info(
                                        "【{}】发送缓存积压字节数：【{}】",
                                        channel,
                                        channel.unsafe().outboundBuffer().totalPendingWriteBytes());
                            }

                            log.info("读取速率(字节/分)：【{}】", totalReadBytes.getAndSet(0));
                            log.info("写出速率(字节/分)：【{}】", totalWriteBytes.getAndSet(0));
                            log.info("----------------性能指标采集结束-------------------");
                        }
                    },
                    0,
                    60 * 1000,
                    TimeUnit.MILLISECONDS);
        }
        channelGroup.add(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        int readableBytes = ((ByteBuf) msg).readableBytes();
        totalReadBytes.getAndAdd(readableBytes);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
            throws Exception {
        int writeableBytes = ((ByteBuf) msg).readableBytes();
        totalWriteBytes.getAndAdd(writeableBytes);
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        chCount.decrementAndGet();
        channelGroup.remove(ctx.channel());
        super.channelInactive(ctx);
    }
}
