package com.imokkkk.serializable.protobuf;

import com.imokkkk.serializable.protobuf.MyMessageOuterClass.MyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author liuwy
 * @date 2023-04-24 15:45
 * @since 1.0
 */
public class ProtoBufServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MyMessage message = (MyMessage) msg;
        System.out.println(message.getName());
    }
}
