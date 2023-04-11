package com.imokkkk.encrypt.compression.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023/4/11 13:09
 * @since 1.0
 */
@Slf4j
public class MyServer {
    public static void main(String[] args) throws InterruptedException {
        MyServer myServer = new MyServer();
        myServer.start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(7777)
                    .childHandler(new MyServerInitializer());
            // 异步绑定到服务器，sync()会阻塞到完成
            ChannelFuture channelFuture = bootstrap.bind().sync();
            log.info("服务端启动完成！");
            // 阻塞当前线程，直到服务器的ServerChannel被关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }
}
