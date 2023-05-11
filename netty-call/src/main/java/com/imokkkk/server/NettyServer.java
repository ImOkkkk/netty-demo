package com.imokkkk.server;

import com.imokkkk.kryo.KryoDecoder;
import com.imokkkk.kryo.KryoEncoder;
import com.imokkkk.server.handler.HeartBeatRespHandler;
import com.imokkkk.server.handler.LoginAuthRespHandler;
import com.imokkkk.server.handler.MetricsHandler;
import com.imokkkk.server.handler.ServerBizHandler;
import com.imokkkk.server.processor.DefaultTaskProcessor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-09 15:09
 * @since 1.0
 */
@Slf4j
public class NettyServer {
    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 8989;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        EventLoopGroup workerGroup =
                new NioEventLoopGroup(
                        NettyRuntime.availableProcessors(), new DefaultThreadFactory("worker"));
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(new MetricsHandler());
                                    pipeline.addLast(
                                            new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                                    pipeline.addLast(new LengthFieldPrepender(2));

                                    pipeline.addLast(new KryoDecoder());
                                    pipeline.addLast(new KryoEncoder());
                                    // 心跳超时
                                    pipeline.addLast(new ReadTimeoutHandler(15));

                                    pipeline.addLast(new LoginAuthRespHandler());
                                    pipeline.addLast(new HeartBeatRespHandler());
                                    pipeline.addLast(
                                            new ServerBizHandler(new DefaultTaskProcessor()));
                                }
                            });
            ChannelFuture future = b.bind(SERVER_PORT).sync();
            log.info("Netty Server start!");
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }
}
