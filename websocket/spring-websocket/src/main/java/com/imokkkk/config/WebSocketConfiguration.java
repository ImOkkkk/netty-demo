package com.imokkkk.config;

import com.imokkkk.websocket.IWebSocketHandler;
import com.imokkkk.websocket.IWebSocketShakeInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author liuwy
 * @date 2023-05-08 11:33
 * @since 1.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this.webSocketHandler(), "/") // 配置处理器
                .addInterceptors(new IWebSocketShakeInterceptor()) // 配置拦截器
                .setAllowedOrigins("*"); // 解决跨域问题
    }

    @Bean
    public IWebSocketHandler webSocketHandler() {
        return new IWebSocketHandler();
    }

    @Bean
    public IWebSocketShakeInterceptor webSocketShakeInterceptor() {
        return new IWebSocketShakeInterceptor();
    }
}
