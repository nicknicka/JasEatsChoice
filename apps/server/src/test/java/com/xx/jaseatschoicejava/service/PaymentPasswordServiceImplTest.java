package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.constants.VerificationType;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.service.impl.PaymentPasswordServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付密码重置必须通过短信验证码校验，否则任何人都能绕过验证重置他人支付密码。
 */
@ExtendWith(MockitoExtension.class)
class PaymentPasswordServiceImplTest {

    private static final String USER_ID = "10086";
    private static final String PHONE = "13800000000";

    @Mock
    private UserMapper userMapper;

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private PaymentPasswordServiceImpl paymentPasswordService;

    private User existingUser() {
        User user = new User();
        user.setUserId(USER_ID);
        user.setPhone(PHONE);
        user.setHasPaymentPassword(true);
        return user;
    }

    @Test
    void resetPaymentPassword_withoutVerificationCode_shouldRejectAndNotWrite() {
        when(userMapper.selectOne(any())).thenReturn(existingUser());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentPasswordService.resetPaymentPassword(USER_ID, "654321", " "));

        assertEquals("短信验证码不能为空", ex.getMessage());
        verify(verificationService, never()).verifyCode(anyString(), anyString(), anyString());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void resetPaymentPassword_withWrongVerificationCode_shouldRejectAndNotWrite() {
        when(userMapper.selectOne(any())).thenReturn(existingUser());
        when(verificationService.verifyCode(PHONE, "000000", VerificationType.PAYMENT)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentPasswordService.resetPaymentPassword(USER_ID, "654321", "000000"));

        assertEquals("短信验证码错误或已过期", ex.getMessage());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void resetPaymentPassword_withValidVerificationCode_shouldResetPassword() {
        User user = existingUser();
        when(userMapper.selectOne(any())).thenReturn(user);
        when(verificationService.verifyCode(PHONE, "123456", VerificationType.PAYMENT)).thenReturn(true);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        paymentPasswordService.resetPaymentPassword(USER_ID, "654321", "123456");

        assertTrue(new BCryptPasswordEncoder().matches("654321", user.getPaymentPassword()));
        assertTrue(user.getHasPaymentPassword());
        verify(userMapper).updateById(user);
    }
}
