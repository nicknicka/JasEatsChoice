package com.xx.jaseatschoicejava.agent.config;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 在 AI/Agent 请求入口固定当时的模型客户端，保证热切换不影响执行中的请求。
 */
@Component
@Order(100)
public class AiModelRequestScopeFilter extends OncePerRequestFilter {

    private final DynamicAiModelRegistry registry;

    public AiModelRequestScopeFilter(DynamicAiModelRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !(path.startsWith("/v1/ai") || path.startsWith("/agent/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        registry.pinForRequest(request);
        filterChain.doFilter(request, response);
    }
}
