package com.imokkkk.splicing.delimiter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 包尾增加分隔符解决粘包、半包
 *
 * @author liuwy
 * @date 2023-04-14 15:39
 * @since 1.0
 */
@Slf4j
public class DelimiterServer {
    public static final String DELIMITER_SYMBOL = "@~";

    public static void main(String[] args) throws InterruptedException {
        DelimiterServer delimiterServer = new DelimiterServer();
        delimiterServer.start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        final DelimiterServerHandler delimiterServerHandler = new DelimiterServerHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(7777)
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ByteBuf delimiter =
                                            Unpooled.copiedBuffer(DELIMITER_SYMBOL.getBytes());
                                    ch.pipeline()
                                            .addLast(
                                                    new DelimiterBasedFrameDecoder(1024, delimiter))
                                            .addLast(delimiterServerHandler);
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
