package com.imokkkk.kryo;

import com.imokkkk.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author liuwy
 * @date 2023-05-06 13:22
 * @since 1.0
 */
public class KryoEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        out.writeBytes(KryoSerializer.serialize(msg));
        ctx.flush();
    }
}
