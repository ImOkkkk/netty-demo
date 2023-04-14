package com.imokkkk.splicing.fixed;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息定长解决粘包、半包
 *
 * @author liuwy
 * @date 2023-04-14 15:39
 * @since 1.0
 */
@Slf4j
public class FixedLengthServer {
    public static final String RESPONSE = "Welcome to Netty!";

    public static void main(String[] args) throws InterruptedException {
        FixedLengthServer fixedLengthServer = new FixedLengthServer();
        fixedLengthServer.start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        final FixedLengthServerHandler fixedLengthServerHandler = new FixedLengthServerHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(7777)
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline()
                                            .addLast(
                                                    new FixedLengthFrameDecoder(
                                                            FixedLengthClient.REQUEST.length()))
                                            .addLast(fixedLengthServerHandler);
                                }
                            });
            ChannelFuture f = b.bind().sync();
            log.info("服务端启动完成...");
            f.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
