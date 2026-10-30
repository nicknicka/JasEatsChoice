package com.xx.jaseatschoicejava.agent.context;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 从签名身份和服务端绑定关系解析 AI 聊天上下文。
 */
@Slf4j
@Service
public class AgentChatContextResolver {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AgentChatContextResolver(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public Optional<AgentChatContext> resolve(HttpServletRequest request) {
        String authorization = request == null ? null : request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            return Optional.empty();
        }

        String userId;
        try {
            userId = jwtUtil.extractUserId(token);
        } catch (Exception e) {
            log.debug("AI聊天上下文身份解析失败: {}", e.getMessage());
            return Optional.empty();
        }

        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }

        try {
            User user = userService.getById(userId);
            if (user == null || !userService.isLoginAllowed(user)) {
                return Optional.empty();
            }

            String merchantId = user.getMerchantId();
            if (merchantId != null && !merchantId.isBlank()) {
                return Optional.of(AgentChatContext.merchant(userId, merchantId));
            }
            return Optional.of(AgentChatContext.customer(userId));
        } catch (RuntimeException e) {
            log.warn("AI聊天上下文用户绑定解析失败: userId={}", userId, e);
            return Optional.empty();
        }
    }
}
