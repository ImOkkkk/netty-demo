package com.imokkkk.encrypt.compression.clinet;

import com.imokkkk.encrypt.compression.common.MyDecryptHandler;
import com.imokkkk.encrypt.compression.common.MyEncryptHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;

/**
 * @author liuwy
 * @date 2023/4/11 13:14
 * @since 1.0
 */
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        pipeline.addLast(new MyDecryptHandler());
        pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        pipeline.addLast(new MyEncryptHandler());
        pipeline.addLast(new MyClientBusinessLogicHandler());
    }
}
