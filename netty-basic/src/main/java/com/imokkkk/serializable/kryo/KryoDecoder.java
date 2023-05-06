package com.imokkkk.serializable.kryo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author liuwy
 * @date 2023-05-06 13:29
 * @since 1.0
 */
public class KryoDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        String message = KryoSerializer.deSerialize(bytes, String.class);
        out.add(message);
    }
}
