package com.imokkkk.push.netty;

import com.alibaba.fastjson.JSON;
import com.imokkkk.push.model.Message;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author liuwy
 * @date 2023-05-06 14:52
 * @since 1.0
 */
@Component
@Sharable
public class IWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(IWebSocketHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        NettyContext.group.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {
        log.info("服务器收到消息：{}", msg.text());
        // 获取用户ID
        Message message = JSON.parseObject(msg.text(), Message.class);
        // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        ctx.channel().attr(key).setIfAbsent(message.getUserId());
        // 关联channel
        NettyContext.addChannel(message.getUserId(), ctx.channel());
        // 回复消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器连接成功！"));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("channel：【{}】，自动关闭连接", ctx.channel().id().asLongText());
        // 删除通道
        NettyContext.group.remove(ctx.channel());
        removeUserId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("发生异常：{}", cause.getMessage());
        // 删除通道
        NettyContext.group.remove(ctx.channel());
        removeUserId(ctx);
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     *
     * @param ctx
     */
    private void removeUserId(ChannelHandlerContext ctx) {
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = ctx.channel().attr(key).get();
        NettyContext.removeChannel(userId);
    }
}
