package com.imokkkk.ai;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023/4/7 17:09
 * @since 1.0
 */
@Slf4j
public class SimpleAIServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立连接！");
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // 接收客户端发送的消息
        String msgStr = msg.toString(CharsetUtil.UTF_8);
        log.info("Client：{}", msgStr);
        // 回复消息给客户端
        String response = msgStr.replaceAll("吗", "").replaceAll("\\?", "!").replaceAll("？", "！");

        // NOTE
        //如果调用Channel或者ChannelPipeline上的这些方法，它们将沿着整个ChannelPipeline进行传播。
        //而调用位于ChannelHandlerContext上的相同方法，则将从当前所关联的 ChannelHandler 开始，并且只会传播给位于该
        // ChannelPipeline 中的下一个（入站下一个，出站上一个）能够处理该事件的 ChannelHandler。
        // ctx.writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端异常：{}", cause.getMessage(), cause);
        ctx.close();
    }
}
