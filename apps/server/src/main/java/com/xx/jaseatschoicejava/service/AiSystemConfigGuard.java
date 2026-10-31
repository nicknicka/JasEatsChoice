package com.xx.jaseatschoicejava.service;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 保护 AI 运行时配置不被通用系统配置权限读写。
 */
@Component
public class AiSystemConfigGuard {

    private static final String AI_PERMISSION = "admin:setting:ai";
    private static final String SUPER_PERMISSION = "admin:super";
    private static final String AI_RUNTIME_CONFIG_KEY = "ai.runtime-config";

    public boolean isAiRuntimeConfig(String configKey, String configGroup) {
        return AI_RUNTIME_CONFIG_KEY.equals(configKey) && "ai".equals(configGroup);
    }

    public void requireAiRuntimeAccess(String configKey, String configGroup) {
        if (!isAiRuntimeConfig(configKey, configGroup)) {
            return;
        }
        if (!canAccessAiRuntimeConfig()) {
            throw new org.springframework.security.access.AccessDeniedException("需要 admin:setting:ai 权限");
        }
    }

    public boolean canAccessAiRuntimeConfig() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        List<? extends GrantedAuthority> authorities = List.copyOf(authentication.getAuthorities());
        return authorities.stream().anyMatch(authority ->
            AI_PERMISSION.equals(authority.getAuthority())
                || SUPER_PERMISSION.equals(authority.getAuthority()));
    }
}
