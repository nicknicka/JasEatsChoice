package com.xx.jaseatschoicejava.filter;

import com.xx.jaseatschoicejava.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器
 * 从请求头中提取 JWT token 并验证，设置认证信息到 SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头中获取 Authorization
        String authorizationHeader = request.getHeader("Authorization");

        // ========== 【调试日志已注释】 ==========
        // System.out.println("=== JWT Filter Debug ===");
        // System.out.println("Request URI: " + request.getRequestURI());
        // System.out.println("Authorization Header: " + (authorizationHeader != null ? authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())) + "..." : "null"));

        // 检查是否有 Bearer token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                // 验证 token
                String phone = jwtUtil.extractPhone(token);
                // System.out.println("Extracted phone: " + phone);

                if (phone != null) {
                    // 检查 token 是否有效
                    if (!jwtUtil.isTokenExpired(token)) {
                        // System.out.println("Token is valid, extracting permissions");
                        // 构建权限列表
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                        // 从 token 中提取权限列表
                        List<String> permissions = jwtUtil.extractPermissions(token);
                        // System.out.println("Extracted permissions: " + (permissions != null ? permissions.toString() : "null"));
                        if (permissions != null && !permissions.isEmpty()) {
                            // 添加细粒度权限
                            for (String permission : permissions) {
                                authorities.add(new SimpleGrantedAuthority(permission));
                            }
                            // System.out.println("Loaded " + permissions.size() + " permissions from token");
                        }

                        // 添加 ADMIN 角色
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        // System.out.println("Total authorities: " + authorities.size());

                        // 创建认证信息
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(phone, null, authorities);

                        // 将认证信息设置到 SecurityContext（每次都重新设置）
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        // System.out.println("Authentication set successfully with ROLE_ADMIN and " + (permissions != null ? permissions.size() : 0) + " permissions");
                    } else {
                        // System.out.println("Token expired, returning 401");
                        // Token过期，返回401错误
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\": 401, \"message\": \"Token已过期，请重新登录\"}");
                        return; // 阻止继续处理
                    }
                }
            } catch (Exception e) {
                // token 无效或过期
                // System.out.println("JWT token validation failed: " + e.getMessage());

                // 如果是过期异常，返回401
                if (e.getMessage() != null && e.getMessage().contains("expired")) {
                    // System.out.println("Token expired, returning 401");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\": 401, \"message\": \"Token已过期，请重新登录\"}");
                    return; // 阻止继续处理
                }

                logger.debug("JWT token validation failed: " + e.getMessage());
            }
        } else {
            // System.out.println("No Bearer token found");
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}
