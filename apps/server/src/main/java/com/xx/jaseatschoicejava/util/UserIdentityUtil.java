package com.xx.jaseatschoicejava.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 用户身份识别工具类。
 * <p>
 * 唯一可信来源是 {@code JwtAuthenticationFilter} 验签通过后写入的请求属性；
 * 不再采信 {@code X-User-Id} 等可伪造请求头，也不在工具类里第二次解析令牌。
 * 未携带有效令牌时一律返回 null，由调用方按需转成 401。
 */
public class UserIdentityUtil {

    /**
     * 请求属性名：JwtAuthenticationFilter 校验通过 JWT 后写入的可信用户ID。
     */
    public static final String VERIFIED_USER_ID_ATTRIBUTE = "jaseats.verifiedUserId";

    /**
     * 从请求中获取用户ID。
     * <p>
     * 仅为兼容既有调用方保留的方法名，语义等同 {@link #extractVerifiedUserId(HttpServletRequest)}。
     *
     * @param request HTTP请求
     * @return 已验证的用户ID，未登录或令牌无效时返回 null
     */
    public static String extractUserId(HttpServletRequest request) {
        return extractVerifiedUserId(request);
    }

    /**
     * 从请求中获取可信用户ID：只认 JwtAuthenticationFilter 校验通过的 JWT 写入的用户ID。
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

    /**
     * 取已认证身份；未登录或令牌无效时直接 401。
     *
     * @param request HTTP请求
     * @return 已验证的用户ID（非 null）
     * @throws ResponseStatusException 未登录或令牌无效
     */
    public static String requireVerifiedUserId(HttpServletRequest request) {
        String userId = extractVerifiedUserId(request);
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
