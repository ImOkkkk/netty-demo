package com.imokkkk.server.handler;

import com.imokkkk.model.Message;
import com.imokkkk.model.MessageType;
import com.imokkkk.util.SecurityCenter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-09 16:18
 * @since 1.0
 */
@Slf4j
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        if (MessageType.HEARTBEAT_REQ.same(message.getHeader())) {
            Message resp = new Message();
            resp = resp.buildBasicMessage(0, MessageType.HEARTBEAT_RESP);
            log.debug("心跳应答：【{}】", resp);
            ctx.writeAndFlush(resp);
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("客户端断开连接");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            log.warn("客户端长时间未响应，断开连接！");
            SecurityCenter.removeLoginUser(ctx.channel().remoteAddress().toString());
            ctx.close();
        }
        super.exceptionCaught(ctx, cause);
    }
}
