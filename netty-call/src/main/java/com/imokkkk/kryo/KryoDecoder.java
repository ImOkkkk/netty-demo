package com.imokkkk.kryo;


import com.imokkkk.model.Message;
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
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        Message message = KryoSerializer.deSerialize(bytes, Message.class);
        out.add(message);
    }
}
