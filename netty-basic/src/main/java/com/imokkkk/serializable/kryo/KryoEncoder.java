package com.imokkkk.serializable.kryo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author liuwy
 * @date 2023-05-06 13:22
 * @since 1.0
 */
public class KryoEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        out.writeBytes(KryoSerializer.serialize(((ByteBuf) msg).toString(CharsetUtil.UTF_8)));
        ctx.flush();
    }
}
