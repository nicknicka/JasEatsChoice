package com.xx.jaseatschoicejava.security;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 超级权限投票器
 * 如果用户拥有 admin:super 权限，则自动允许访问所有需要权限的接口
 */
public class SuperPermissionVoter implements AccessDecisionVoter<Object> {

    private static final String SUPER_PERMISSION = "admin:super";

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ACCESS_DENIED;
        }

        // 检查用户是否拥有超级权限
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                if (SUPER_PERMISSION.equals(authority.getAuthority())) {
                    // 有超级权限，允许访问
                    return ACCESS_GRANTED;
                }
            }
        }

        // 没有超级权限，弃权，让其他投票器决定
        return ACCESS_ABSTAIN;
    }
}
