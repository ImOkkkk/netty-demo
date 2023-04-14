package com.imokkkk.splicing.fixed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-04-14 15:36
 * @since 1.0
 */
@Slf4j
public class FixedLengthClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private AtomicInteger counter = new AtomicInteger(0);

    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info(
                "{} and the counter is: {}",
                msg.toString(CharsetUtil.UTF_8),
                counter.incrementAndGet());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf msg = null;
        for (int i = 0; i < 10; i++) {
            msg = Unpooled.buffer(FixedLengthClient.REQUEST.length());
            msg.writeBytes(FixedLengthClient.REQUEST.getBytes());
            ctx.writeAndFlush(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
