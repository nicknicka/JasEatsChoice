package com.xx.jaseatschoicejava.agent.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.JwtUtil;

class AgentChatContextResolverTest {

    private final UserService userService = mock(UserService.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final AgentChatContextResolver resolver = new AgentChatContextResolver(userService, jwtUtil);

    {
        when(userService.isLoginAllowed(any())).thenReturn(true);
    }

    @Test
    void resolvesCustomerContextFromSignedIdentityOnly() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        request.addHeader("X-User-Id", "999");

        when(jwtUtil.extractUserId("token")).thenReturn("1001");
        when(userService.getById("1001")).thenReturn(user("1001", null));

        Optional<AgentChatContext> context = resolver.resolve(request);

        assertTrue(context.isPresent());
        assertEquals("1001", context.get().getUserId());
        assertEquals("customer", context.get().getAudience());
        assertEquals(null, context.get().getMerchantId());
    }

    @Test
    void resolvesMerchantContextFromServerSideBinding() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.extractUserId("token")).thenReturn("2002");
        when(userService.getById("2002")).thenReturn(user("2002", "m-2002"));

        Optional<AgentChatContext> context = resolver.resolve(request);

        assertTrue(context.isPresent());
        assertEquals("2002", context.get().getUserId());
        assertEquals("merchant", context.get().getAudience());
        assertEquals("m-2002", context.get().getMerchantId());
    }

    @Test
    void failsClosedWithoutSignedIdentity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "999");

        assertTrue(resolver.resolve(request).isEmpty());
    }

    @Test
    void failsClosedForUnknownUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");

        when(jwtUtil.extractUserId("invalid")).thenReturn("404");
        when(userService.getById("404")).thenReturn(null);

        assertTrue(resolver.resolve(request).isEmpty());
    }

    @Test
    void failsClosedForDisabledUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        User user = user("1001", null);
        user.setStatus(false);
        when(jwtUtil.extractUserId("token")).thenReturn("1001");
        when(userService.getById("1001")).thenReturn(user);
        when(userService.isLoginAllowed(user)).thenReturn(false);

        assertTrue(resolver.resolve(request).isEmpty());
    }

    @Test
    void merchantContextIsIncompleteWithoutMerchantBinding() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.extractUserId("token")).thenReturn("1001");
        when(userService.getById("1001")).thenReturn(user("1001", " "));

        Optional<AgentChatContext> context = resolver.resolve(request);

        assertTrue(context.isPresent());
        assertEquals("customer", context.get().getAudience());
        assertFalse(context.get().isMerchant());
        assertEquals(null, context.get().getMerchantId());
    }

    private User user(String userId, String merchantId) {
        User user = new User();
        user.setUserId(userId);
        user.setStatus(true);
        user.setMerchantId(merchantId);
        return user;
    }
}
