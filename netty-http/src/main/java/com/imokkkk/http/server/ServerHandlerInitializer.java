package com.imokkkk.http.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;

/**
 * @author liuwy
 * @date 2023-04-22 21:46
 * @since 1.0
 */
public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslContext;

    public ServerHandlerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(ch.alloc()));
        }
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("decoder", new HttpRequestDecoder());

        //聚合http为一个完整的报文
        pipeline.addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
        //压缩应答报文
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast(new ServerBizHandler());
    }
}
