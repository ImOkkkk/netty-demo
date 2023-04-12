package com.imokkkk.encrypt.compression.clinet;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author liuwy
 * @date 2023/4/11 13:48
 * @since 1.0
 */
public class MyClient {

    public static void main(String[] args) throws Exception {
        MyClient client = new MyClient();
        client.run();
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new MyClientInitializer());

            ChannelFuture f = b.connect("127.0.0.1", 7777).sync();

            // 键盘输入消息，发送至服务端
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }
                ByteBuf buf = f.channel().alloc().buffer();
                buf.writeBytes(message.getBytes(Charset.forName("UTF-8")));
                f.channel().writeAndFlush(buf);
            }
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
