package com.imokkkk.serializable.kryo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author liuwy
 * @date 2023/4/7 17:22
 * @since 1.0
 */
@Slf4j
public class KryoClient {

    public static void main(String[] args) throws InterruptedException, IOException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(new KryoEncoder());
                                    pipeline.addLast(new KryoDecoder());
                                    pipeline.addLast(new KryoClientHandler());
                                }
                            });
            ChannelFuture f = bootstrap.connect("127.0.0.1", 9632).sync();

            f.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
