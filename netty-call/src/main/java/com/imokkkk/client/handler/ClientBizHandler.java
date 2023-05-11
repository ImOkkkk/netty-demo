package com.imokkkk.client.handler;

import com.imokkkk.model.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-11 11:26
 * @since 1.0
 */
@Slf4j
public class ClientBizHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        log.info("业务应答消息：【{}】", msg.toString());
    }
}
