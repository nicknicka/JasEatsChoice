package com.xx.jaseatschoicejava.config;

import com.xx.jaseatschoicejava.security.SuperPermissionVoter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法级别安全配置
 * 配置自定义的 AccessDecisionManager，使 @PreAuthorize 注解支持超级权限
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    /**
     * 重写 accessDecisionManager 方法，使用自定义的决策管理器
     * 这样 @PreAuthorize 注解就会使用我们的 SuperPermissionVoter
     */
    @Override
    protected AccessDecisionManager accessDecisionManager() {
        // 创建投票器列表
        List<org.springframework.security.access.AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();

        // 添加 PreAuthorize 认证投票器（处理 @PreAuthorize 注解）
        decisionVoters.add(new PreInvocationAuthorizationAdviceVoter(
            preInvocationAuthorizationAdvice()));

        // 添加超级权限投票器（优先级最高）
        decisionVoters.add(new SuperPermissionVoter());

        // 添加基于角色的投票器
        RoleVoter roleVoter = new RoleVoter();
        roleVoter.setRolePrefix(""); // 不使用前缀
        decisionVoters.add(roleVoter);

        // 添加已认证用户投票器（处理 isAuthenticated() 等表达式）
        decisionVoters.add(new AuthenticatedVoter());

        // 使用肯定性投票策略（只要有一个投票器同意就通过）
        AffirmativeBased affirmativeBased = new AffirmativeBased(decisionVoters);
        affirmativeBased.setAllowIfAllAbstainDecisions(false); // 所有投票器弃权时拒绝
        return affirmativeBased;
    }
}
