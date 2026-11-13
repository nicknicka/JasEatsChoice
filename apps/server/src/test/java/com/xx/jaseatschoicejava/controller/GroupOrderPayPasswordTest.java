package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.PaymentPasswordService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.UserCouponService;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * 群订单支付入口：身份只认令牌，支付密码缺失或错误时不得创建流水、扣款或推进群订单状态。
 */
class GroupOrderPayPasswordTest {

    private GroupOrderService groupOrderService;
    private PaymentService paymentService;
    private UserCouponService userCouponService;
    private WalletService walletService;
    private PaymentPasswordService paymentPasswordService;
    private GroupOrderController controller;

    @BeforeEach
    void setUp() {
        groupOrderService = mock(GroupOrderService.class);
        paymentService = mock(PaymentService.class);
        userCouponService = mock(UserCouponService.class);
        walletService = mock(WalletService.class);
        paymentPasswordService = mock(PaymentPasswordService.class);

        controller = new GroupOrderController();
        ReflectionTestUtils.setField(controller, "groupOrderService", groupOrderService);
        ReflectionTestUtils.setField(controller, "paymentService", paymentService);
        ReflectionTestUtils.setField(controller, "userCouponService", userCouponService);
        ReflectionTestUtils.setField(controller, "walletService", walletService);
        ReflectionTestUtils.setField(controller, "paymentPasswordService", paymentPasswordService);

        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setId("group-order-1");
        groupOrder.setStatus(1);
        groupOrder.setMerchantId("M1");
        when(groupOrderService.getGroupOrderDetail("group-order-1")).thenReturn(groupOrder);
        when(groupOrderService.getGroupOrderRuntimeState(groupOrder, "user-1"))
                .thenReturn(Map.of("currentUserJoined", true, "locked", true));
        when(groupOrderService.getSettlement("group-order-1", "user-1"))
                .thenReturn(Map.of("totalAmount", new BigDecimal("10.00")));
    }

    @Test
    void missingPasswordIsRejectedBeforeAnyMoneyMoves() {
        ResponseResult<?> result = controller.payGroupOrder(
                "group-order-1",
                Map.of("paymentMethod", "balance"),
                verifiedRequest("user-1")
        );

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
        assertEquals("请输入支付密码", result.getMessage());
        verifyNoInteractions(paymentService, userCouponService, walletService);
        verify(groupOrderService, never()).refreshGroupOrderPaymentState(anyString(), anyString(), any(), anyString());
    }

    @Test
    void wrongPasswordIsRejectedBeforeAnyMoneyMoves() {
        when(paymentPasswordService.verifyPaymentPassword("user-1", "000000")).thenReturn(false);

        ResponseResult<?> result = controller.payGroupOrder(
                "group-order-1",
                Map.of("paymentMethod", "balance", "paymentPassword", "000000"),
                verifiedRequest("user-1")
        );

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
        assertEquals("支付密码错误", result.getMessage());
        verifyNoInteractions(paymentService, userCouponService, walletService);
        verify(groupOrderService, never()).refreshGroupOrderPaymentState(anyString(), anyString(), any(), anyString());
    }

    @Test
    void anonymousCallerIsRejected() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.payGroupOrder(
                        "group-order-1",
                        Map.of("paymentMethod", "balance", "paymentPassword", "123456"),
                        new MockHttpServletRequest())
        );

        assertEquals(401, exception.getStatusCode().value());
        verifyNoInteractions(paymentService, walletService);
    }

    private MockHttpServletRequest verifiedRequest(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, userId);
        return request;
    }
}
