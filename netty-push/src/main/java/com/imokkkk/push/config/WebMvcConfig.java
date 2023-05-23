package com.imokkkk.push.config;

import com.imokkkk.push.interceptor.ResourceBulkheadHandlerInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author liuwy
 * @date 2023-05-23 10:36
 * @since 1.0
 */
@Configuration
@Import(value = {ResourceBulkheadHandlerInterceptor.class})
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired private List<HandlerInterceptor> handlerInterceptors;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        handlerInterceptors.forEach(registry::addInterceptor);
    }
}
