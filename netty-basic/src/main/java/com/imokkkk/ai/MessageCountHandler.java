package com.imokkkk.ai;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-04-12 13:44
 * @since 1.0
 *
 * 统计服务端接受到和发出的业务报文总数
 */
@Slf4j
@Sharable //可以被多个 Channel(处理多个网络连接时，就会存在多个 Channel 实例) 共享使用的 ChannelHandler
public class MessageCountHandler extends ChannelDuplexHandler {
    private AtomicLong inCount = new AtomicLong(0);
    private AtomicLong outCount = new AtomicLong(0);

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        log.info("收到报文总数：" + inCount.incrementAndGet());
        super.read(ctx);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        log.info("发送报文总数：" + outCount.incrementAndGet());
        super.flush(ctx);
    }
}
