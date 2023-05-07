package com.imokkkk.push.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuwy
 * @date 2023-05-06 14:59
 * @since 1.0
 */
public class NettyContext {
    /** 定义一个channel组，管理所有的channel GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例 */
    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /** 存放用户与Chanel的对应信息，用于给指定用户发送消息 */
    private static ConcurrentHashMap<String, Channel> userId2Channel = new ConcurrentHashMap<>();

    /**
     * 获取用户channel
     *
     * @return
     */
    public static Channel getChannel(String userId) {
        return userId2Channel.get(userId);
    }

    public static void addChannel(String userId, Channel channel) {
        userId2Channel.put(userId, channel);
    }

    public static void removeChannel(String userId) {
        userId2Channel.remove(userId);
    }
}
