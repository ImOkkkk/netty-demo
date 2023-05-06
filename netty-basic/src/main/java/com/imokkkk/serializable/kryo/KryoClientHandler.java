package com.imokkkk.serializable.kryo;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-06 13:35
 * @since 1.0
 */
@Slf4j
public class KryoClientHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 接收服务端发送的消息
        log.info("Server：{}", msg.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello Netty！(from Client)", CharsetUtil.UTF_8));
    }
}
