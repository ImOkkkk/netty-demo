package com.imokkkk.http.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-04-22 22:15
 * @since 1.0
 */
@Slf4j
public class ClientBizHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        URI uri = new URI("/test");
        String msg = "Hello";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
          HttpMethod.GET, uri.toASCIIString(),
          Unpooled.wrappedBuffer(msg.getBytes(CharsetUtil.UTF_8)));
        request.headers().set(HttpHeaderNames.HOST, HttpClient.HOST);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        // 发送http请求
        ctx.writeAndFlush(request);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = (FullHttpResponse) msg;
        log.info("response status：【{}】，headers：【{}】，response：【{}】", response.status(),
          response.headers(), response.content().toString(CharsetUtil.UTF_8));
        response.release();
    }
}
