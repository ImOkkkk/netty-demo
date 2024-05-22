package com.imokkkk.demo.chat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.SocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author wyliu
 * @date 2024/5/22 10:14
 * @since 1.0
 */
public class ChatHolder {
    static final Map<SocketChannel, String> USER_MAP = new ConcurrentHashMap<>();

    public static void join(SocketChannel socketChannel) {
        String userId = "用户" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        send(socketChannel, "您的id为：" + userId + "\n\r");
        USER_MAP.put(socketChannel, userId);
        propagate(socketChannel, userId + "加入聊天室\n\r");
    }

    public static void quit(SocketChannel socketChannel) {
        String userId = USER_MAP.get(socketChannel);
        send(socketChannel, "您离开聊天室\n\r");
        propagate(socketChannel, userId + "离开聊天室\n\r");
        USER_MAP.remove(socketChannel);
    }

    public static void propagate(SocketChannel socketChannel, String content) {
        String userId = USER_MAP.get(socketChannel);
        for (SocketChannel channel : USER_MAP.keySet()) {
            if (channel != socketChannel) {
                send(channel, userId + "：" + content);
            }
        }
    }

    public static void send(SocketChannel socketChannel, String msg) {
        try {
            ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
            ByteBuf writeBuffer = allocator.buffer(msg.getBytes().length);
            writeBuffer.writeCharSequence(msg, StandardCharsets.UTF_8);
            socketChannel.writeAndFlush(writeBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
