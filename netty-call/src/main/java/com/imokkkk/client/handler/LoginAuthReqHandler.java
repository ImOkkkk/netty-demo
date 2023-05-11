package com.imokkkk.client.handler;

import cn.hutool.core.lang.id.NanoId;
import com.imokkkk.model.Message;
import com.imokkkk.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-05-11 11:08
 * @since 1.0
 */
@Slf4j
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        /*发出认证请求*/
        Message message = new Message();
        message = message.buildBasicMessage(null, MessageType.LOGIN_REQ);
        log.info("请求服务器认证：【{}】", message);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        if (MessageType.LOGIN_REQ.same(message.getHeader())) {
            log.info("收到认证应答报文，服务器是否验证通过......");
            byte loginResult = (byte) message.getBody();
            if (loginResult != (byte) 0) {
                /*握手失败，关闭连接*/
                log.warn("未通过认证，关闭连接：【{}】", message);
                ctx.close();
            } else {
                log.info("通过认证，移除本处理器，进入业务通信：【{}】", message);
                ctx.pipeline().remove(this);
                ReferenceCountUtil.release(msg);
            }
        }else {
            ctx.fireChannelRead(msg);
        }
    }
}
