package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.OrderCreateDTO;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.PaymentPasswordService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * 下单入口的身份与金额边界。金额重算本身在 {@link com.xx.jaseatschoicejava.util.OrderPricingTest} 里覆盖。
 */
class OrderControllerTrustTest {

    private OrderService orderService;
    private PaymentService paymentService;
    private WalletService walletService;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        paymentService = mock(PaymentService.class);
        walletService = mock(WalletService.class);
        controller = new OrderController(
                orderService,
                paymentService,
                walletService,
                mock(OrderDishService.class),
                mock(PaymentPasswordService.class)
        );
    }

    @Test
    void createOrderUsesAuthenticatedIdentityAndIgnoresBodyAmount() {
        Order order = new Order();
        order.setId("order-1");
        order.setUserId("victim");
        order.setMerchantId("M1");
        order.setTotalAmount(new BigDecimal("0.01"));
        OrderDish item = new OrderDish();
        item.setDishId("D1");
        item.setQuantity(1);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setOrder(order);
        dto.setDishes(List.of(item));

        when(orderService.createOrderWithDishes(any(Order.class), anyList(), anyString())).thenReturn(true);

        ResponseResult<?> result = controller.createOrder(dto, verifiedRequest("attacker"));

        assertEquals("200", result.getCode());
        // 下单人取令牌身份，订单金额由 service 重算，controller 不采信请求体
        verify(orderService).createOrderWithDishes(eq(order), anyList(), eq("attacker"));
    }

    @Test
    void createOrderRejectsAnonymousCaller() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setOrder(new Order());

        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.createOrder(dto, new MockHttpServletRequest()));

        assertEquals(401, exception.getStatusCode().value());
        verify(orderService, never()).createOrderWithDishes(any(Order.class), anyList(), anyString());
    }

    @Test
    void payOrderRejectsPayingSomeoneElsesOrder() {
        Order order = new Order();
        order.setId("order-2");
        order.setUserId("victim");
        order.setStatus(0);
        order.setTotalAmount(new BigDecimal("10.00"));
        when(orderService.getById("order-2")).thenReturn(order);

        ResponseResult<?> result = controller.payOrder("order-2", "victim", "wallet", "123456", verifiedRequest("attacker"));

        assertEquals("403", result.getCode());
        assertTrue(!result.getSuccess());
        verify(paymentService, never()).createPayment(anyString(), anyString(), anyString(), any(), anyString());
    }

    private MockHttpServletRequest verifiedRequest(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, userId);
        return request;
    }
}
