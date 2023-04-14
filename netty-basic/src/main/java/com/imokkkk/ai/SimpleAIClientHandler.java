package com.imokkkk.ai;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023/4/7 17:25
 * @since 1.0
 */
@Slf4j
public class SimpleAIClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg)
            throws Exception {
        //接收服务端发送的消息
        log.info("Server：{}", msg.toString(CharsetUtil.UTF_8));
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello Netty！(from Client)", CharsetUtil.UTF_8));
        ctx.alloc().buffer();
//        super.channelActive(ctx);
    }

}
