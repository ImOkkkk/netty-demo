package com.imokkkk.http.client;

import com.imokkkk.http.server.HttpServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * @author liuwy
 * @date 2023-04-22 22:11
 * @since 1.0
 */
public class HttpClient {

    public static final String HOST = "127.0.0.1";


    public void connect(String host, int port) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup).channel(NioSocketChannel.class)
              .handler(new ChannelInitializer<SocketChannel>() {
                  @Override
                  protected void initChannel(SocketChannel ch) throws Exception {
                      ch.pipeline().addLast(new HttpClientCodec());
                      ch.pipeline()
                        .addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
                      ch.pipeline().addLast("decompressor", new HttpContentDecompressor());
                      ch.pipeline().addLast(new ClientBizHandler());

                  }
              });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (HttpServer.SSL) {
            System.out.println("服务器处于SSL模式，本客户端不支持，退出");
            return;
        }
        HttpClient client = new HttpClient();
        client.connect("127.0.0.1", HttpServer.port);
    }
}
