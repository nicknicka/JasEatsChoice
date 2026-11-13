package com.xx.jaseatschoicejava.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.xx.jaseatschoicejava.util.JwtUtil;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 角色授予的边界：只有带 admin:* 权限的管理端令牌才拿 ROLE_ADMIN。
 */
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "signed-token";
    private static final String USER_ID = "1001";

    private JwtAuthenticationFilter filter;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtUtil = Mockito.mock(JwtUtil.class);
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);

        when(jwtUtil.extractPhone(anyString())).thenReturn("17300000000");
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
        when(jwtUtil.extractUserId(anyString())).thenReturn(USER_ID);
    }

    @Test
    void ordinaryUserTokenNeverGetsAdminRole() throws Exception {
        when(jwtUtil.extractPermissions(TOKEN)).thenReturn(null);

        MockHttpServletRequest request = bearerRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authorities(authentication).contains("ROLE_USER"));
        assertFalse(authorities(authentication).contains("ROLE_ADMIN"));
        // 身份只来自验签结果
        assertTrue("1001".equals(request.getAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE)));
    }

    @Test
    void tokenWithNonAdminPermissionsNeverGetsAdminRole() throws Exception {
        when(jwtUtil.extractPermissions(TOKEN)).thenReturn(List.of("user:order:read"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(bearerRequest(), response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertFalse(authorities(authentication).contains("ROLE_ADMIN"));
    }

    @Test
    void adminTokenGetsAdminRole() throws Exception {
        when(jwtUtil.extractPermissions(TOKEN)).thenReturn(List.of("admin:user:list", "admin:order:list"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(bearerRequest(), response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authorities(authentication).contains("ROLE_ADMIN"));
        assertTrue(authorities(authentication).contains("admin:user:list"));
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        when(jwtUtil.isTokenExpired(TOKEN)).thenReturn(true);

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(bearerRequest(), response, new MockFilterChain());

        assertTrue(response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockHttpServletRequest bearerRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        return request;
    }

    private List<String> authorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
