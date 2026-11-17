package com.xx.jaseatschoicejava.security;

import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 调用方身份解析：给"这条资源是不是你的"提供唯一判据。
 *
 * <p>背景（#597）：商家端不少接口只用路径/请求体里的 merchantId 定位资源，从不校验它是否属于
 * 当前登录者，于是任何登录用户都能读写别人的商家数据。判据散落在各控制器里必然再次漂移，
 * 因此统一收敛到这里。
 *
 * <p>身份只有两个可信来源：{@code JwtAuthenticationFilter} 验签后写入的
 * {@code jaseats.verifiedUserId}（用户ID），以及用户档案上的 {@code usr_user.merchant_id}
 * （非空表示该账号已注册为商家）。请求头、请求体里的任何 id 都不可信。
 *
 * <p>注意 {@code ai_chat_session.user_id} 与 {@code soc_wish_list_item} 的归属方可能是用户ID
 * 也可能是商家ID（同一账号的两种身份），所以这里提供的是"任一身份匹配"的判据，
 * 而不是单一 merchantId 相等。
 */
@Component
@RequiredArgsConstructor
public class CurrentIdentityResolver {

    private final UserService userService;

    /**
     * 已验签的调用方用户ID；未登录返回 null（给"登录即可"的接口用）。
     */
    public String userIdOrNull(HttpServletRequest request) {
        return UserIdentityUtil.extractVerifiedUserId(request);
    }

    /**
     * 已验签的调用方用户ID；未登录直接 401。
     */
    public String requireUserId(HttpServletRequest request) {
        return UserIdentityUtil.requireVerifiedUserId(request);
    }

    /**
     * 调用方用户档案；未登录或用户不存在返回 null。
     */
    public User currentUserOrNull(HttpServletRequest request) {
        String userId = userIdOrNull(request);
        return userId == null ? null : userService.getById(userId);
    }

    /**
     * 调用方绑定的商家ID；未登录、用户不存在或该账号不是商家时返回 null。
     */
    public String merchantIdOrNull(HttpServletRequest request) {
        User user = currentUserOrNull(request);
        return user == null ? null : user.getMerchantId();
    }

    /**
     * 是否管理员：沿用既有口径，认安全管理器里 {@code admin:} 前缀的权限。
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority() != null
                        && authority.getAuthority().startsWith("admin:"));
    }

    /**
     * 调用方的任一身份（用户ID 或商家ID）是否等于给定值。
     *
     * <p>用于归属方可能是两种身份之一的资源，例如 {@code ai_chat_session.user_id}
     * 在用户端会话里是用户ID、在商家端会话里是商家ID。
     *
     * @param request  HTTP请求
     * @param identity 资源上记录的归属方，null/空一律不匹配
     * @return 管理员，或任一身份匹配时返回 true
     */
    public boolean isCallerIdentity(HttpServletRequest request, String identity) {
        if (identity == null || identity.isBlank()) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }

        String userId = userIdOrNull(request);
        if (userId != null && userId.equals(identity)) {
            return true;
        }

        String merchantId = merchantIdOrNull(request);
        return merchantId != null && merchantId.equals(identity);
    }

    /**
     * 资源同时记录了用户归属与商家归属时（例如想吃列表项），任一匹配即可访问。
     *
     * @param request         HTTP请求
     * @param ownerUserId     资源上记录的用户ID，可为 null
     * @param ownerMerchantId 资源上记录的商家ID，可为 null
     */
    public boolean canAccessOwnedResource(HttpServletRequest request, String ownerUserId, String ownerMerchantId) {
        return isCallerIdentity(request, ownerUserId) || isCallerIdentity(request, ownerMerchantId);
    }

    /**
     * 能否管理该商家：管理员，或用户档案绑定的 merchantId 与目标一致。
     *
     * @param request    HTTP请求
     * @param merchantId 目标商家ID
     */
    public boolean canManageMerchant(HttpServletRequest request, String merchantId) {
        return canManageMerchant(userIdOrNull(request), merchantId);
    }

    /**
     * 能否管理该商家：调用方已经是解析好的用户ID时用这个重载。
     *
     * @param currentUserId 已验签的调用方用户ID
     * @param merchantId    目标商家ID
     */
    public boolean canManageMerchant(String currentUserId, String merchantId) {
        if (merchantId == null || merchantId.isBlank()) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        if (currentUserId == null || currentUserId.isBlank()) {
            return false;
        }
        User user = userService.getById(currentUserId);
        return user != null && merchantId.equals(user.getMerchantId());
    }
}
