package com.xx.jaseatschoicejava.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

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
    void ignoresBlankVerifiedIdentity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, "   ");

        assertNull(UserIdentityUtil.extractVerifiedUserId(request));
    }
}
