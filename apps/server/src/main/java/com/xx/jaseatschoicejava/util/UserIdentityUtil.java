package com.xx.jaseatschoicejava.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户身份识别工具类
 * 从请求中提取用户ID，支持多种方式
 */
public class UserIdentityUtil {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    // JWT工具类实例（Spring注入）
    private static JwtUtil jwtUtil;

    /**
     * 设置JWT工具类实例（由Spring调用）
     */
    public static void setJwtUtil(JwtUtil util) {
        jwtUtil = util;
    }

    /**
     * 从请求中获取用户ID
     * 优先级：X-User-Id请求头 > JWT Token解析
     *
     * @param request HTTP请求
     * @return 用户ID，如果无法获取则返回null
     */
    public static String extractUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 1. 优先从X-User-Id请求头获取（前端传递）
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId != null && !userId.isEmpty() && !"null".equals(userId)) {
            return userId;
        }

        // 2. 从JWT Token中解析（备选方案）
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil != null) {
                    String userIdFromToken = jwtUtil.extractUserId(token);
                    if (userIdFromToken != null && !userIdFromToken.isEmpty()) {
                        return userIdFromToken;
                    }
                }
            } catch (Exception e) {
                // JWT解析失败，返回null
                return null;
            }
        }

        return null;
    }

    /**
     * 从请求中获取用户ID（Long类型）
     *
     * @param request HTTP请求
     * @return 用户ID，如果无法获取则返回null
     */
    public static Long extractUserIdAsLong(HttpServletRequest request) {
        String userId = extractUserId(request);
        if (userId != null) {
            try {
                return Long.parseLong(userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
