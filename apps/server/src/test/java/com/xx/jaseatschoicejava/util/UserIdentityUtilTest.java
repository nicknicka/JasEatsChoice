package com.xx.jaseatschoicejava.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class UserIdentityUtilTest {

    @Test
    void ignoresClientSuppliedUserIdAndOnlyUsesVerifiedRequestAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "forged-user");
        request.addHeader("Authorization", "Bearer forged-token");

        assertNull(UserIdentityUtil.extractVerifiedUserId(request));

        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, "42");
        assertEquals("42", UserIdentityUtil.extractVerifiedUserId(request));
    }

    @Test
    void extractUserIdNeverFallsBackToForgedHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "forged-user");

        // 伪造请求头不再有任何优先级：没有令牌验签结果就是未登录
        assertNull(UserIdentityUtil.extractUserId(request));
        assertNull(UserIdentityUtil.extractUserIdAsLong(request));

        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, "7");
        assertEquals("7", UserIdentityUtil.extractUserId(request));
        assertEquals(Long.valueOf(7L), UserIdentityUtil.extractUserIdAsLong(request));
    }

    @Test
    void ignoresBlankVerifiedIdentity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, "   ");

        assertNull(UserIdentityUtil.extractVerifiedUserId(request));
    }

    @Test
    void requireVerifiedUserIdThrows401WhenAnonymous() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "forged-user");

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> UserIdentityUtil.requireVerifiedUserId(request));
        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void requireVerifiedUserIdReturnsVerifiedIdentity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, "1024");

        assertEquals("1024", UserIdentityUtil.requireVerifiedUserId(request));
    }
}
