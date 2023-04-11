package com.imokkkk.encrypt.compression.common;

import cn.hutool.core.codec.Base64;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * @author liuwy
 * @date 2023/4/11 13:41
 * @since 1.0
 */
public class MyDecryptHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        System.out.println("MyDecryptHandler...");
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        byte[] decode = Base64.decode(bytes);
        out.add(decode);
    }
}
