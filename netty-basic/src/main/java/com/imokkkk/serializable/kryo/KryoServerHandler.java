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
public class KryoServerHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 接收服务端发送的消息
        log.info("Client：{}", msg.toString());

        // 回复客户端
        ctx.channel()
                .writeAndFlush(Unpooled.copiedBuffer("Hello！(from Server)", CharsetUtil.UTF_8));
    }
}
