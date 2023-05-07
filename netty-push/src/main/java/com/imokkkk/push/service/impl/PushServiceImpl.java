package com.imokkkk.push.service.impl;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.imokkkk.push.constant.Constants;
import com.imokkkk.push.model.Message;
import com.imokkkk.push.netty.NettyContext;
import com.imokkkk.push.service.PushService;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liuwy
 * @date 2023-05-06 15:21
 * @since 1.0
 */
@Service
public class PushServiceImpl implements PushService {

    @Autowired private RedisTemplate redisTemplate;

    @Override
    public void push(Message message) {
        // 推送给具体的用户
        if (StrUtil.isNotBlank(message.getUserId())) {
            Channel channel = NettyContext.getChannel(message.getUserId());
            if (channel != null) {
                // 如果该用户的客户端是与本服务器建立的channel，直接推送消息
                channel.writeAndFlush(new TextWebSocketFrame(message.getMessageText()));
            } else {
                redisTemplate.convertAndSend(Constants.PUSH_MESSAGE, message);
            }
        } else {
            // 全局推送
            redisTemplate.convertAndSend(Constants.PUSH_MESSAGE, message);
        }
    }
}
