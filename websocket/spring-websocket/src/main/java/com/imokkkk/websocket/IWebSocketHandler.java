package com.imokkkk.websocket;

import com.imokkkk.handler.MessageHandler;
import com.imokkkk.model.AuthRequest;
import com.imokkkk.util.WebSocketUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuwy
 * @date 2023-05-08 11:28
 * @since 1.0
 */
public class IWebSocketHandler extends TextWebSocketHandler implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 消息类型与 MessageHandler 的映射
     *
     * <p>无需设置成静态变量
     */
    private final Map<String, MessageHandler> HANDLERS = new HashMap<>();

    @Autowired private ApplicationContext applicationContext;

    @Override // 对应 open 事件
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("[afterConnectionEstablished][session({}) 接入]", session);
        // 解析 accessToken
        String accessToken = (String) session.getAttributes().get("accessToken");
        // 创建 AuthRequest 消息类型
        AuthRequest authRequest = new AuthRequest().setAccessToken(accessToken);
        // 获得消息处理器
        MessageHandler<AuthRequest> messageHandler = HANDLERS.get(AuthRequest.TYPE);
        if (messageHandler == null) {
            logger.error("[onOpen][认证消息类型，不存在消息处理器]");
            return;
        }
        messageHandler.execute(session, authRequest);
    }

    @Override // 对应 message 事件
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage)
            throws Exception {
        logger.info(
                "[handleMessage][session({}) 接收到一条消息({})]",
                session,
                textMessage); // 生产环境下，请设置成 debug 级别
    }

    @Override // 对应 close 事件
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        logger.info("[afterConnectionClosed][session({}) 连接关闭。关闭原因是({})}]", session, status);
        WebSocketUtil.removeSession(session);
    }

    @Override // 对应 error 事件
    public void handleTransportError(WebSocketSession session, Throwable exception)
            throws Exception {
        logger.info("[handleTransportError][session({}) 发生异常]", session, exception);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 通过 ApplicationContext 获得所有 MessageHandler Bean
        applicationContext
                .getBeansOfType(MessageHandler.class)
                .values() // 获得所有 MessageHandler Bean
                .forEach(
                        messageHandler ->
                                HANDLERS.put(
                                        messageHandler.getType(),
                                        messageHandler)); // 添加到 handlers 中
        logger.info("[afterPropertiesSet][消息处理器数量：{}]", HANDLERS.size());
    }
}
