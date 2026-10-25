package com.xx.jaseatschoicejava.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理员上下文工具类
 * 从请求中提取JWT token并获取管理员信息
 */
public class AdminContext {

    private static final String ADMIN_TOKEN_HEADER = "Authorization";
    private static final String ADMIN_TOKEN_PREFIX = "Bearer ";

    /**
     * 获取当前请求的HttpServletRequest
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 从请求头中获取JWT token
     */
    public static String getToken() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String bearerToken = request.getHeader(ADMIN_TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(ADMIN_TOKEN_PREFIX)) {
            return bearerToken.substring(ADMIN_TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 从token中获取管理员ID
     * 注意：这里使用的是userId字段，管理员登录时存入的是adminId的字符串形式
     */
    public static Long getAdminId() {
        String token = getToken();
        if (token == null) {
            return null;
        }

        try {
            // 这里需要通过Spring Bean获取JwtUtil
            // 由于静态方法无法直接注入Bean，需要通过ApplicationContext获取
            JwtUtil jwtUtil = SpringContextHolder.getBean(JwtUtil.class);
            String userIdStr = jwtUtil.extractUserId(token);
            return userIdStr != null ? Long.parseLong(userIdStr) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从token中获取管理员用户名
     */
    public static String getAdminUsername() {
        String token = getToken();
        if (token == null) {
            return null;
        }

        try {
            JwtUtil jwtUtil = SpringContextHolder.getBean(JwtUtil.class);
            return jwtUtil.extractPhone(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否是管理员请求
     */
    public static boolean isAdminRequest() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri != null && uri.contains("/admin");
    }
}
