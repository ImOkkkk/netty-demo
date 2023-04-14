package com.imokkkk.encrypt.compression.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
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
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer("Welcome！", CharsetUtil.UTF_8)).sync();
            // 客户端第一次连接登录以后，进行授权检查，检查通过后就可以把这个授权 handler 移除了。
            // 如果客户端关闭连接下线，下次再连接的时候，就是一个新的连接，授权 handler 依然会被安装到 ChannelPipeline ，依然会进行授权检查。
            ctx.pipeline().remove(this);
            // 验证通过
            super.channelRead(ctx, msg);
        } else {
            // 验证未通过，返回验证失败消息，关闭连接
            ctx.writeAndFlush(Unpooled.copiedBuffer("账号或密码错误！", CharsetUtil.UTF_8)).sync();
            ctx.close();
        }
    }

    private String[] decodeUsernamePassword(Object msg) throws UnsupportedEncodingException {
        // 将消息转换为字符串，格式为"username:password"
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                String str = new String(bytes, "UTF-8");
                return str.split(":");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }finally{
                ReferenceCountUtil.release(buf);
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
