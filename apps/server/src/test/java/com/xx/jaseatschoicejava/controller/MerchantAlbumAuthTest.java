package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 商家相册删除：与上传保持同一口径，非商家本人且非管理员一律 403，且不得触达数据库。
 */
class MerchantAlbumAuthTest {

    private JdbcTemplate jdbcTemplate;
    private MerchantController controller;

    @BeforeEach
    void setUp() {
        UserService userService = mock(UserService.class);
        jdbcTemplate = mock(JdbcTemplate.class);

        User otherUser = new User();
        otherUser.setUserId("other-user");
        otherUser.setMerchantId("M2");
        when(userService.getById("other-user")).thenReturn(otherUser);

        controller = new MerchantController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "jdbcTemplate", jdbcTemplate);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deletingOthersAlbumIsForbidden() {
        ResponseResult<?> result = controller.deleteMerchantAlbum(
                "M1",
                "http://cdn.example.com/album/1.jpg",
                "environment",
                verifiedRequest("other-user")
        );

        assertFalse(result.getSuccess());
        assertEquals("403", result.getCode());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void deletingWithoutTokenIsRejected() {
        ResponseResult<?> result = controller.deleteMerchantAlbum(
                "M1",
                "http://cdn.example.com/album/1.jpg",
                "environment",
                new MockHttpServletRequest()
        );

        assertFalse(result.getSuccess());
        assertEquals("401", result.getCode());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void administratorMayDeleteAnyMerchantAlbum() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin", null, AuthorityUtils.createAuthorityList("admin:all"))
        );

        // 管理员越过归属判定，进入查库环节；此处不关心后续 SQL 结果，只确认不再被 403 拦住
        ResponseResult<?> result = controller.deleteMerchantAlbum(
                "M1",
                "http://cdn.example.com/album/1.jpg",
                "environment",
                verifiedRequest("admin")
        );

        assertFalse("403".equals(result.getCode()));
    }

    private MockHttpServletRequest verifiedRequest(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, userId);
        return request;
    }
}
