package com.imokkkk.ai;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author liuwy
 * @date 2023/4/10 15:13
 * @since 1.0
 */
public class SimpleAIClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new SimpleAIClientHandler());
    }
}
