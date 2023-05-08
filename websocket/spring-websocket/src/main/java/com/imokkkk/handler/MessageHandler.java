package com.imokkkk.handler;

import com.imokkkk.model.Message;

import javax.websocket.Session;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author liuwy
 * @date 2023-05-08 10:31
 * @since 1.0
 */
public interface MessageHandler<T extends Message> {
    /**
     * 执行处理消息
     *
     * @param session 会话
     * @param message 消息
     */
    void execute(WebSocketSession session, T message);

    /**
     * @return 消息类型，即每个 Message 实现类上的 TYPE 静态字段
     */
    String getType();
}
