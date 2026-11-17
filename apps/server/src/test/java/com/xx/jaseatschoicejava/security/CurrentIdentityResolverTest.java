package com.xx.jaseatschoicejava.security;

import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 调用方身份判据测试（#597）。
 *
 * <p>覆盖五种身份：未登录、纯用户账号、商家账号、同时是用户与商家的账号、管理员。
 */
@ExtendWith(MockitoExtension.class)
class CurrentIdentityResolverTest {

    private static final String USER_ID = "3384650106421960";
    private static final String MERCHANT_ID = "7638432224340229";
    private static final String OTHER_MERCHANT_ID = "9999999999999";

    @Mock
    private UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CurrentIdentityResolver resolver() {
        return new CurrentIdentityResolver(userService);
    }

    private MockHttpServletRequest authenticatedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, USER_ID);
        return request;
    }

    private User userWithMerchant(String merchantId) {
        User user = new User();
        user.setUserId(USER_ID);
        user.setMerchantId(merchantId);
        return user;
    }

    private void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, AuthorityUtils.createAuthorityList("admin:all")));
    }

    @Test
    void unauthenticatedRequestHasNoIdentity() {
        CurrentIdentityResolver resolver = resolver();

        assertNull(resolver.userIdOrNull(new MockHttpServletRequest()));
        assertNull(resolver.currentUserOrNull(new MockHttpServletRequest()));
        assertNull(resolver.merchantIdOrNull(new MockHttpServletRequest()));
        assertFalse(resolver.isAdmin());
    }

    @Test
    void pureUserAccountHasNoMerchantId() {
        when(userService.getById(USER_ID)).thenReturn(userWithMerchant(null));
        CurrentIdentityResolver resolver = resolver();

        assertNull(resolver.merchantIdOrNull(authenticatedRequest()));
        // 纯用户账号既管不了商家，也不是商家身份
        assertFalse(resolver.canManageMerchant(authenticatedRequest(), MERCHANT_ID));
        assertTrue(resolver.isCallerIdentity(authenticatedRequest(), USER_ID));
        assertFalse(resolver.isCallerIdentity(authenticatedRequest(), MERCHANT_ID));
    }

    @Test
    void merchantAccountCanOnlyManageItsOwnMerchant() {
        when(userService.getById(USER_ID)).thenReturn(userWithMerchant(MERCHANT_ID));
        CurrentIdentityResolver resolver = resolver();
        MockHttpServletRequest request = authenticatedRequest();

        assertTrue(resolver.canManageMerchant(request, MERCHANT_ID));
        assertFalse(resolver.canManageMerchant(request, OTHER_MERCHANT_ID));
        assertFalse(resolver.canManageMerchant(request, null));
        assertFalse(resolver.canManageMerchant(request, "  "));
    }

    /**
     * 同一账号同时是用户与商家（种子账号 17322222222 就是这种），
     * 会话与想吃项的归属方可能是两种身份之一，因此两种都要认。
     */
    @Test
    void accountThatIsBothUserAndMerchantMatchesEitherIdentity() {
        when(userService.getById(USER_ID)).thenReturn(userWithMerchant(MERCHANT_ID));
        CurrentIdentityResolver resolver = resolver();
        MockHttpServletRequest request = authenticatedRequest();

        assertTrue(resolver.isCallerIdentity(request, USER_ID));
        assertTrue(resolver.isCallerIdentity(request, MERCHANT_ID));
        assertFalse(resolver.isCallerIdentity(request, "someone-else"));
        assertFalse(resolver.isCallerIdentity(request, null));
    }

    @Test
    void ownedResourceIsAccessibleWhenEitherOwnerMatches() {
        when(userService.getById(USER_ID)).thenReturn(userWithMerchant(MERCHANT_ID));
        CurrentIdentityResolver resolver = resolver();
        MockHttpServletRequest request = authenticatedRequest();

        assertTrue(resolver.canAccessOwnedResource(request, USER_ID, null));
        assertTrue(resolver.canAccessOwnedResource(request, null, MERCHANT_ID));
        assertTrue(resolver.canAccessOwnedResource(request, "someone-else", MERCHANT_ID));
        assertFalse(resolver.canAccessOwnedResource(request, "someone-else", OTHER_MERCHANT_ID));
        assertFalse(resolver.canAccessOwnedResource(request, null, null));
    }

    @Test
    void adminBypassesOwnershipChecks() {
        authenticateAsAdmin();
        CurrentIdentityResolver resolver = resolver();
        MockHttpServletRequest request = authenticatedRequest();

        assertTrue(resolver.isAdmin());
        assertTrue(resolver.canManageMerchant(request, OTHER_MERCHANT_ID));
        assertTrue(resolver.isCallerIdentity(request, "someone-else"));
        assertTrue(resolver.canAccessOwnedResource(request, "someone-else", OTHER_MERCHANT_ID));
        // 管理员重载同样放行，且不需要查库
        assertTrue(resolver.canManageMerchant(USER_ID, OTHER_MERCHANT_ID));
    }

    @Test
    void anonymousAuthenticationIsNotAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        CurrentIdentityResolver resolver = resolver();

        assertFalse(resolver.isAdmin());
        assertFalse(resolver.canManageMerchant(authenticatedRequest(), MERCHANT_ID));
    }

    @Test
    void nonAdminAuthorityThatIsNotAdminPrefixDoesNotBypass() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "someone", null, AuthorityUtils.createAuthorityList("ROLE_USER", "merchant:read")));
        when(userService.getById(USER_ID)).thenReturn(userWithMerchant(MERCHANT_ID));
        CurrentIdentityResolver resolver = resolver();

        assertFalse(resolver.isAdmin());
        assertTrue(resolver.canManageMerchant(authenticatedRequest(), MERCHANT_ID));
        assertFalse(resolver.canManageMerchant(authenticatedRequest(), OTHER_MERCHANT_ID));
    }

    @Test
    void missingUserIdArgumentNeverManagesMerchant() {
        CurrentIdentityResolver resolver = resolver();

        assertFalse(resolver.canManageMerchant((String) null, MERCHANT_ID));
        assertFalse(resolver.canManageMerchant("  ", MERCHANT_ID));
        assertFalse(resolver.canManageMerchant(USER_ID, null));
        assertTrue(List.of().isEmpty());
    }
}
