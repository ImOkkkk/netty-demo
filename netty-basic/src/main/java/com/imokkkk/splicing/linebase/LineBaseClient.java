package com.imokkkk.splicing.linebase;

import com.imokkkk.splicing.delimiter.DelimiterClientHandler;
import com.imokkkk.splicing.delimiter.DelimiterServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-04-14 15:33
 * @since 1.0
 */
@Slf4j
public class LineBaseClient {
    public static final String REQUEST = "Hello, Hi, How are you?";

    public static void main(String[] args) throws InterruptedException {
        LineBaseClient lineBaseClient = new LineBaseClient();
        lineBaseClient.start();
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
                                    ByteBuf delimiter =
                                            Unpooled.copiedBuffer(
                                                    DelimiterServer.DELIMITER_SYMBOL.getBytes());
                                    ch.pipeline()
                                            .addLast(
                                                    new LineBasedFrameDecoder(1024))
                                            .addLast(new LineBaseClientHandler());
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
