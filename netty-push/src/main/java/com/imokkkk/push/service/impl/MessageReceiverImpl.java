package com.imokkkk.push.service.impl;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.imokkkk.push.model.Message;
import com.imokkkk.push.netty.NettyContext;
import com.imokkkk.push.service.MessageReceiver;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * @author liuwy
 * @date 2023-05-06 16:00
 * @since 1.0
 */
@Component
@Slf4j
public class MessageReceiverImpl implements MessageReceiver {

    @Override
    public void receive(String message) {
        log.info(message);
        Message messageDTO = JSON.parseObject(message, Message.class);
        if (StrUtil.isNotBlank(messageDTO.getUserId())) {
            Channel channel = NettyContext.getChannel(messageDTO.getUserId());
            channel.writeAndFlush(new TextWebSocketFrame(messageDTO.getMessageText()));
        } else {
            NettyContext.group.writeAndFlush(new TextWebSocketFrame(messageDTO.getMessageText()));
        }
    }
}
