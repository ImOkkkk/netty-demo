package com.imokkkk.serializable.protobuf;

import com.imokkkk.serializable.protobuf.MyMessageOuterClass.MyMessage;
import com.imokkkk.serializable.protobuf.MyMessageOuterClass.MyMessage.Builder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author liuwy
 * @date 2023-04-24 16:02
 * @since 1.0
 */
public class ProtoBufClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Prepare to make data........");
        Builder builder = MyMessage.newBuilder();
        builder.setName("Mark");
        builder.setId(1);
        System.out.println("send data........");
        ctx.writeAndFlush(builder.build());
    }
}
