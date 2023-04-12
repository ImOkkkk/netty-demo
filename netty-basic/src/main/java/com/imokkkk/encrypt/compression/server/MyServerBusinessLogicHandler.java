package com.imokkkk.encrypt.compression.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023/4/11 13:45
 * @since 1.0
 */
@Slf4j
public class MyServerBusinessLogicHandler extends SimpleChannelInboundHandler<byte[]> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端异常：{}", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        System.out.println("MyServerBusinessLogicHandler...");

        // 处理业务逻辑
        String msgStr = new String(msg, CharsetUtil.UTF_8);
        log.info("{}！from Client", msgStr);

        // 回复客户端
        ctx.writeAndFlush(Unpooled.copiedBuffer(String.format("%s！from Server", msgStr), CharsetUtil.UTF_8))
                .sync();
    }
}
