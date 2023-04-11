package com.imokkkk.encrypt.compression.common;

import cn.hutool.core.codec.Base64;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

/**
 * @author liuwy
 * @date 2023/4/11 13:30
 * @since 1.0
 */
public class MyEncryptHandler extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println("MyEncryptHandler...");
        if (msg instanceof ByteBuf) {
            String encode = Base64.encode(((ByteBuf) msg).toString(CharsetUtil.UTF_8));
            out.writeBytes(encode.getBytes());
        } else if (msg instanceof byte[]) {
            String encode = Base64.encode((byte[]) msg);
            out.writeBytes(encode.getBytes());
        }
    }
}
