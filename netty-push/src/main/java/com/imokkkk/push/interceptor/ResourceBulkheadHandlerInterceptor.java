package com.imokkkk.push.interceptor;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author liuwy
 * @date 2023-05-23 10:16
 * @since 1.0
 */
public class ResourceBulkheadHandlerInterceptor
        implements HandlerInterceptor,
                InitializingBean,
                ApplicationListener<ContextRefreshedEvent> {

    private BulkheadConfig config;

    private Map<Method, Bulkhead> methodBulkheadsMapping;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.config = BulkheadConfig.custom().build();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.methodBulkheadsMapping = new HashMap<>();
        ApplicationContext context = contextRefreshedEvent.getApplicationContext();
        Map<String, RequestMappingHandlerMapping> requestMappingHandlerMappingMap =
                context.getBeansOfType(RequestMappingHandlerMapping.class);
        for (RequestMappingHandlerMapping requestMappingHandlerMapping :
                requestMappingHandlerMappingMap.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                    requestMappingHandlerMapping.getHandlerMethods();
            for (Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo requestMappingInfo = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();
                Method method = handlerMethod.getMethod();
                String resourceName = requestMappingInfo.toString();
                methodBulkheadsMapping.put(method, Bulkhead.of(resourceName, config));
            }
        }
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Bulkhead bulkhead = doGetBulkhead(handlerMethod);
            if (bulkhead != null) {
                bulkhead.acquirePermission();
            }
        }
        return true;
    }

    private Bulkhead doGetBulkhead(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        return methodBulkheadsMapping.get(method);
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView)
            throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Bulkhead bulkhead = doGetBulkhead(handlerMethod);
            if (bulkhead != null) {
                bulkhead.releasePermission();
            }
        }
    }
}
