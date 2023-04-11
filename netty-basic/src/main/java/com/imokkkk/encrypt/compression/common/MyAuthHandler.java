package com.imokkkk.encrypt.compression.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import java.io.UnsupportedEncodingException;

/**
 * @author liuwy
 * @date 2023/4/11 13:35
 * @since 1.0
 */
public class MyAuthHandler extends ChannelInboundHandlerAdapter {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "123456";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("MyAuthHandler...");

        // 进行用户名密码验证
        String[] userCredentials = decodeUsernamePassword(msg);
        if (userCredentials != null && checkCredentials(userCredentials[0], userCredentials[1])) {
            // 验证通过，设置验证标记
            ctx.channel().attr(AttributeKey.valueOf("auth")).set(true);
            super.channelRead(ctx, msg);
        } else {
            // 验证未通过，返回验证失败消息，关闭连接
            ctx.writeAndFlush(Unpooled.copiedBuffer("账号或密码错误！", CharsetUtil.UTF_8));
            ctx.close();
        }
    }

    private String[] decodeUsernamePassword(Object msg) throws UnsupportedEncodingException {
        // 将消息转换为字符串，格式为"username:password"
        if (msg instanceof ByteBuf) {
            try {
                ByteBuf buf = (ByteBuf) msg;
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                String str = new String(bytes, "UTF-8");
                return str.split(":");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else if (msg instanceof byte[]) {
            String str = new String((byte[]) msg, "UTF-8");
            return str.split(":");
        }
        return null;
    }

    private boolean checkCredentials(String username, String password) {
        // 判断用户名和密码是否正确（此处仅做示例，实际使用需要替换为更安全的验证方式）
        return USERNAME.equals(username) && PASSWORD.equals(password);
    }
}
