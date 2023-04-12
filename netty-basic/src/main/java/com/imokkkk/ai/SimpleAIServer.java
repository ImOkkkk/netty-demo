package com.imokkkk.ai;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023/4/7 17:05
 * @since 1.0
 */
@Slf4j
public class SimpleAIServer {
    private void start() throws InterruptedException {
        //线程组
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        final MessageCountHandler messageCountHandler = new MessageCountHandler();
        try {
            //服务端启动必备
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class) //NIO通信方式
                    .localAddress(new InetSocketAddress(7777))
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel socketChannel)
                                        throws Exception {
                                    socketChannel.pipeline().addLast(new SimpleAIServerHandler(), messageCountHandler);
                                }
                            });
            //异步绑定到服务器，sync()会阻塞到完成
            ChannelFuture channelFuture = bootstrap.bind().sync();
            log.info("服务端启动完成！");
            //阻塞当前线程，直到服务器的ServerChannel被关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SimpleAIServer simpleAIServer = new SimpleAIServer();
        simpleAIServer.start();
    }
}
