package com.imokkkk.http.server.autossl;

import com.imokkkk.http.server.ServerBizHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;

/**
 * @author liuwy
 * @date 2023-04-24 13:26
 * @since 1.0
 */
public class AutoSSLServerHandlerInitializer extends ChannelInitializer {
    private SslContext sslContext;

    public AutoSSLServerHandlerInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 根据客户端的访问来决定是否启用SSL
        pipeline.addLast(new OptionalSslHandler(sslContext));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("decoder", new HttpRequestDecoder());

        pipeline.addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
        pipeline.addLast("compressor", new HttpContentCompressor());
        pipeline.addLast(new ServerBizHandler());
    }
}
