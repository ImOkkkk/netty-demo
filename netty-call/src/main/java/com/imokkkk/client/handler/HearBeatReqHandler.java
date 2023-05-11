package com.imokkkk.client.handler;

import com.imokkkk.model.Message;
import com.imokkkk.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-11 11:18
 * @since 1.0
 */
@Slf4j
public class HearBeatReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT) {
            Message message = new Message();
            message = message.buildBasicMessage(null, MessageType.HEARTBEAT_RESP);
            log.debug("写空闲，发送心跳报文维持连接：【{}】", message);
            ctx.writeAndFlush(message);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        if (MessageType.HEARTBEAT_REQ.same(message.getHeader())) {
            log.debug("收到服务器心跳应答，服务器正常！");
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            log.warn("服务器长时间未应答，关闭连接！");
            ctx.close();
        }
        super.exceptionCaught(ctx, cause);
    }
}
