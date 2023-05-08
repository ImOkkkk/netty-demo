package com.imokkkk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author liuwy
 * @date 2023-05-08 10:10
 * @since 1.0
 */
@Configuration
public class WebSocketConfiguration {
    //扫描添加有 @ServerEndpoint 注解的 Bean
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
