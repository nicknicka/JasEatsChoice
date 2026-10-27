package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.MerchantRegisterRequest;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.CaptchaUtil;
import com.xx.jaseatschoicejava.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class MerchantControllerTest {

    @Mock
    private MerchantService merchantService;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CaptchaUtil captchaUtil;

    @InjectMocks
    private MerchantController controller;

    @Test
    void register_withoutAuthorization_shouldFailWithoutWriting() {
        MerchantRegisterRequest request = new MerchantRegisterRequest();
        request.setName("测试商家");
        request.setCaptcha("1234");
        request.setCaptchaKey("captcha-key");

        ResponseResult<?> result = controller.register(request, null);

        assertFalse(result.getSuccess());
        assertEquals("401", result.getCode());
        verify(merchantService, never()).register(any(), anyString());
    }

    @Test
    void register_withInvalidToken_shouldFailWithoutWriting() {
        MerchantRegisterRequest request = new MerchantRegisterRequest();
        request.setName("测试商家");
        request.setCaptcha("1234");
        request.setCaptchaKey("captcha-key");
        when(jwtUtil.extractUserId("bad-token")).thenThrow(new IllegalArgumentException("invalid token"));

        ResponseResult<?> result = controller.register(request, "Bearer bad-token");

        assertFalse(result.getSuccess());
        assertEquals("401", result.getCode());
        verify(merchantService, never()).register(any(), anyString());
    }

    @Test
    void register_withMissingUser_shouldFailWithoutWriting() {
        MerchantRegisterRequest request = new MerchantRegisterRequest();
        request.setName("测试商家");
        request.setCaptcha("1234");
        request.setCaptchaKey("captcha-key");
        when(jwtUtil.extractUserId("valid-token")).thenReturn("user-1");
        when(userService.getById("user-1")).thenReturn(null);

        ResponseResult<?> result = controller.register(request, "Bearer valid-token");

        assertFalse(result.getSuccess());
        assertEquals("401", result.getCode());
        verify(merchantService, never()).register(any(), anyString());
    }

    @Test
    void register_whenAssociationFails_shouldReturnFailure() {
        MerchantRegisterRequest request = new MerchantRegisterRequest();
        request.setName("测试商家");
        request.setBusinessScope(new String[]{"中餐"});
        request.setCaptcha("1234");
        request.setCaptchaKey("captcha-key");
        User user = new User();
        user.setUserId("user-1");

        when(jwtUtil.extractUserId("valid-token")).thenReturn("user-1");
        when(userService.getById("user-1")).thenReturn(user);
        when(captchaUtil.validateCaptchaAndDelete("1234", "captcha-key")).thenReturn(true);
        doThrow(new IllegalStateException("用户与商家关联失败"))
                .when(merchantService).register(any(), anyString());

        ResponseResult<?> result = controller.register(request, "Bearer valid-token");

        assertFalse(result.getSuccess());
        assertEquals("500", result.getCode());
    }
}
