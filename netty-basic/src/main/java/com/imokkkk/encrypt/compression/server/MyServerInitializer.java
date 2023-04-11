package com.imokkkk.encrypt.compression.server;

import com.imokkkk.encrypt.compression.common.MyAuthHandler;
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
public class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 入站事件从头到尾的顺序执行
        ChannelPipeline pipeline = ch.pipeline();
        // 添加解压（入）Handler
        pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));

        // 添加解密（入）Handler
        pipeline.addLast(new MyDecryptHandler());

        // 添加授权（入）Handler
        pipeline.addLast(new MyAuthHandler());

        // 出站事件从尾到头的顺序执行
        // 添加压缩（出）Handler
        pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));

        // 添加加密（出）Handler
        pipeline.addLast(new MyEncryptHandler());

        // 业务逻辑Handler放在最后
        // 添加业务逻辑处理Handler
        pipeline.addLast(new MyServerBusinessLogicHandler());
    }
}
