package com.imokkkk.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * @author liuwy
 * @date 2023-04-21 16:39
 * @since 1.0
 */
public class HttpServer {

    public static final boolean SSL = true;/*是否开启SSL模式*/

    public static final int port = 9999;

    public static void main(String[] args) throws Exception {
        final SslContext sslContext;
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslContext = null;
        }
        try {
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
              .childHandler(new ServerHandlerInitializer(sslContext));
            ChannelFuture f = b.bind(port).sync();
            System.out.println("服务端启动成功，端口是：" + port);
            System.out.println("服务器启动模式：" + (SSL ? "SSL安全模式" : "普通模式"));
            // 监听服务器关闭监听
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
