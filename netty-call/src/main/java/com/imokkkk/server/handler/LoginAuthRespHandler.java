package com.imokkkk.server.handler;

import com.imokkkk.model.Message;
import com.imokkkk.model.MessageType;
import com.imokkkk.util.SecurityCenter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author liuwy
 * @date 2023-05-09 15:20
 * @since 1.0
 */
@Slf4j
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        if (MessageType.LOGIN_REQ.same(message.getHeader())) {
            log.info("收到客户端认证请求：【{}】", message);

            String node = ctx.channel().remoteAddress().toString();
            Message resp = new Message();
            // 重复登录，拒绝
            if (SecurityCenter.isDupLog(node)) {
                resp = resp.buildBasicMessage(-1, MessageType.LOGIN_RESP);
                log.warn("拒绝重复登录：【{}】", resp);
                ctx.writeAndFlush(resp);
                ctx.close();
            } else {
                // FIXME
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                if (SecurityCenter.isWhiteIP(ip)) {
                    SecurityCenter.addLoginUser(node);
                    resp = resp.buildBasicMessage(0, MessageType.LOGIN_RESP);
                    log.info("认证通过：【{}】", resp);
                    ctx.writeAndFlush(resp);
                } else {
                    resp = resp.buildBasicMessage(-1, MessageType.LOGIN_RESP);
                    log.warn("认证失败：【{}】", resp);
                    ctx.writeAndFlush(resp);
                    ctx.close();
                }
            }
            ReferenceCountUtil.release(msg);
        } else {
            // 传递给下一个ChannelInboundHandler
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SecurityCenter.removeLoginUser(ctx.channel().remoteAddress().toString());
        ctx.close();
    }
}
