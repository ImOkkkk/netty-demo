package com.imokkkk.http.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuwy
 * @date 2023-04-22 21:54
 * @since 1.0
 */

@Slf4j
public class ServerBizHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String result = "";
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        try {
            HttpHeaders headers = httpRequest.headers();
            String path = httpRequest.uri();
            String body = httpRequest.content().toString(CharsetUtil.UTF_8);
            HttpMethod method = httpRequest.method();
            log.info("headers：【{}】  path：【{}】  body：【{}】  method：【{}】", headers, path, body,
              method);
            if (!"/test".equalsIgnoreCase(path)) {
                result = "404！" + path;
                send(ctx, result, HttpResponseStatus.NOT_FOUND);
                return;
            }
            if (HttpMethod.GET.equals(method)) {
                result = "GET请求，应答：" + RespConstant.getNews();
                send(ctx, result, HttpResponseStatus.OK);
                return;
            }
        } catch (Exception e) {
            log.info("处理请求失败！", e);
        } finally {
            httpRequest.release();
        }
    }

    private void send(ChannelHandlerContext ctx, String content, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
          Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端：【{}】已连接！", ctx.channel().remoteAddress());
    }
}
