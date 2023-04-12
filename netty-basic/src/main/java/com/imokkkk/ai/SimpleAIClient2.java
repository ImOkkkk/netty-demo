package com.imokkkk.ai;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023/4/7 17:22
 * @since 1.0
 */
@Slf4j
public class SimpleAIClient2 {

    public static void main(String[] args) throws InterruptedException, IOException {
        SimpleAIClient2 simpleAIClient = new SimpleAIClient2();
        simpleAIClient.start();
    }

    private void start() throws InterruptedException, IOException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 7777))
                    .handler(new SimpleAIClientHandler());
            ChannelFuture channelFuture = bootstrap.connect().sync();
            log.info("连接已经建立，您可以开始输入消息！");

            // 键盘输入消息，发送至服务端
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }
                ByteBuf buf = channelFuture.channel().alloc().buffer();
                buf.writeBytes(message.getBytes(Charset.forName("UTF-8")));
                channelFuture.channel().writeAndFlush(buf);
            }
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
