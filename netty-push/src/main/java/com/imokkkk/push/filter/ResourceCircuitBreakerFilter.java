package com.imokkkk.push.filter;

import cn.hutool.core.util.StrUtil;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.apache.catalina.core.ApplicationFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * @author liuwy
 * @date 2023-05-23 9:43
 * @since 1.0
 */
@WebFilter(
        filterName = "resourceCircuitBreakerFilter",
        urlPatterns = "/*",
        dispatcherTypes = {DispatcherType.REQUEST})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResourceCircuitBreakerFilter implements Filter {
    /**
     * org.apache.catalina.core.ApplicationFilterFactory#createFilterChain(javax.servlet.ServletRequest,
     * org.apache.catalina.Wrapper, javax.servlet.Servlet)
     */
    private static final String FILTER_CHAIN_IMPL_CLASS_NAME =
            "org.apache.catalina.core.ApplicationFilterChain";

    private static final Class<?> FILTER_CHAIN_IMPL_CLASS =
            ClassUtils.resolveClassName(FILTER_CHAIN_IMPL_CLASS_NAME, null);

    private CircuitBreakerRegistry circuitBreakerRegistry;
    private Map<String, CircuitBreaker> circuitBreakerCache;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom().build();
        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        this.circuitBreakerCache = new ConcurrentHashMap<>();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String servletName = getServletName(chain);
        if (StrUtil.isBlank(servletName)) {
            chain.doFilter(request, response);
            return;
        }
        CircuitBreaker circuitBreaker =
                circuitBreakerCache.computeIfAbsent(
                        servletName, circuitBreakerRegistry::circuitBreaker);
        circuitBreaker.acquirePermission();
        long start = circuitBreaker.getCurrentTimestamp();
        try {
            chain.doFilter(request, response);
            long duration = circuitBreaker.getCurrentTimestamp() - start;
            circuitBreaker.onSuccess(duration, circuitBreaker.getTimestampUnit());
        } catch (Throwable e) {
            long duration = circuitBreaker.getCurrentTimestamp() - start;
            circuitBreaker.onError(duration, circuitBreaker.getTimestampUnit(), e);
            throw e;
        }
    }

    @Override
    public void destroy() {}

    private String getServletName(FilterChain chain) throws ServletException {
        String servletName = null;
        if (FILTER_CHAIN_IMPL_CLASS != null) {
            ApplicationFilterChain filterChain = (ApplicationFilterChain) chain;
            try {
                Field field = FILTER_CHAIN_IMPL_CLASS.getDeclaredField("servlet");
                field.setAccessible(true);
                Servlet servlet = (Servlet) field.get(filterChain);
                if (servlet != null) {
                    servletName = servlet.getServletConfig().getServletName();
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        return servletName;
    }
}
