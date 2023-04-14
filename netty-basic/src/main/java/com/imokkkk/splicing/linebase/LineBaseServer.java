package com.imokkkk.splicing.linebase;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 自动将接收到的数据流按照指定的分隔符进行切分，并将每个消息封装成一个 ByteBuf 对象交给后续的处理器进行处理。
 *
 * @author liuwy
 * @date 2023-04-14 15:39
 * @since 1.0
 */
@Slf4j
public class LineBaseServer {

    public static void main(String[] args) throws InterruptedException {
        LineBaseServer lineBaseServer = new LineBaseServer();
        lineBaseServer.start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        final LineBaseServerHandler lineBaseServerHandler = new LineBaseServerHandler();
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
                                            .addLast(new LineBasedFrameDecoder(1024))
                                            .addLast(lineBaseServerHandler);
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
