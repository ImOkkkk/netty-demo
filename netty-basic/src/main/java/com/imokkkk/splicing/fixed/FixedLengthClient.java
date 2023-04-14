package com.imokkkk.splicing.fixed;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-04-14 15:33
 * @since 1.0
 */
@Slf4j
public class FixedLengthClient {
    public static final String REQUEST = "Hello, Hi, How are you?";

    public static void main(String[] args) throws InterruptedException {
        FixedLengthClient fixedLengthClient = new FixedLengthClient();
        fixedLengthClient.start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 7777))
                    .handler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline()
                                            .addLast(
                                                    new FixedLengthFrameDecoder(
                                                            FixedLengthServer.RESPONSE.length()))
                                            .addLast(new FixedLengthClientHandler());
                                }
                            });
            ChannelFuture future = b.connect().sync();
            log.info("连接到服务器...");
            future.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
