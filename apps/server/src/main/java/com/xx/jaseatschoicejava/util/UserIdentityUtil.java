package com.xx.jaseatschoicejava.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户身份识别工具类
 * 从请求中提取用户ID，支持多种方式
 */
public class UserIdentityUtil {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * 请求属性名：JwtAuthenticationFilter 校验通过 JWT 后写入的可信用户ID。
     */
    public static final String VERIFIED_USER_ID_ATTRIBUTE = "jaseats.verifiedUserId";

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
     * 从请求中获取可信用户ID：只认 JwtAuthenticationFilter 校验通过的 JWT 写入的用户ID，
     * 不采信任何请求头。内容提取等需要强身份的接口使用该入口；其它模块仍沿用
     * {@link #extractUserId(HttpServletRequest)}。
     *
     * @param request HTTP请求
     * @return 已验证的用户ID，未登录或令牌无效时返回 null
     */
    public static String extractVerifiedUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        Object attribute = request.getAttribute(VERIFIED_USER_ID_ATTRIBUTE);
        if (attribute instanceof String userId && !userId.isBlank()) {
            return userId;
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
