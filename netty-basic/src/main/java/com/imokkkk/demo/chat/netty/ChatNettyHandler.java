package com.imokkkk.demo.chat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import java.nio.charset.StandardCharsets;

/**
 * @author wyliu
 * @date 2024/5/22 10:12
 * @since 1.0
 */
public class ChatNettyHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String content = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(content);
        if ("quit\r\n".equals(content)) {
            ctx.channel().close();
        } else {
            ChatHolder.propagate((SocketChannel) ctx.channel(), content);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("one connection active: " + ctx.channel());
        ChatHolder.join((SocketChannel) ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("one connection inactive: " + ctx.channel());
        ChatHolder.quit((SocketChannel) ctx.channel());
    }
}
