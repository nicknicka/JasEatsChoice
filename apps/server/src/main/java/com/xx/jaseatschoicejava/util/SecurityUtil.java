package com.xx.jaseatschoicejava.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

/**
 * 安全工具类
 * 用于从 Spring Security Context 中获取当前用户信息
 *
 * @Author nickxiao
 * @Date 2025/11/22
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户的手机号
     * @return 手机号，如果未登录返回null
     */
    public static String getCurrentPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // JwtFilter中使用phone作为principal
        }
        return null;
    }

    /**
     * 从请求头中提取当前用户ID
     * 注意：这个方法需要从JWT token中解析，建议在Controller中使用@Autowired注入JwtUtil来获取
     * @return 用户ID
     */
    public static String getCurrentUserId() {
        // 由于Spring Security的Authentication中只存储了phone作为principal
        // 我们需要通过其他方式获取userId
        // 这个方法需要在Controller层面实现，通过HttpServletRequest获取token并解析
        return null;
    }

    /**
     * 检查当前用户是否已认证
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
    }
}
