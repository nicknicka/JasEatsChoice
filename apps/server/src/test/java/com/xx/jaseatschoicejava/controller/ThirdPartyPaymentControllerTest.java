package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.PaymentPasswordService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.UserCouponService;
import com.xx.jaseatschoicejava.service.WalletService;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ThirdPartyPaymentControllerTest {

    @Test
    public void paymentControllerRejectsThirdPartyBeforeCreatingPaymentRecord() {
        OrderService orderService = mock(OrderService.class);
        PaymentService paymentService = mock(PaymentService.class);
        UserCouponService userCouponService = mock(UserCouponService.class);
        PaymentPasswordService paymentPasswordService = mock(PaymentPasswordService.class);
        PaymentController controller =
                new PaymentController(orderService, paymentService, userCouponService, paymentPasswordService);

        ResponseResult<Map<String, Object>> createResult = controller.createPayment(Map.of("paymentMethod", "wechat"));
        ResponseResult<Map<String, Object>> wechatResult = controller.wechatPay(Map.of());
        ResponseResult<Map<String, Object>> alipayResult = controller.alipay(Map.of());

        assertFalse(createResult.getSuccess());
        assertEquals("400", createResult.getCode());
        assertEquals("仅支持平台币支付", createResult.getMessage());

        ResponseResult<Map<String, Object>> balanceAliasResult = controller.createPayment(Map.of(
                "paymentMethod", "balance",
                "orderId", "missing-order",
                "userId", "user-1"
        ));
        assertFalse(balanceAliasResult.getSuccess());
        assertEquals("404", balanceAliasResult.getCode());
        assertEquals("订单不存在", balanceAliasResult.getMessage());

        assertFalse(wechatResult.getSuccess());
        assertEquals("501", wechatResult.getCode());
        assertEquals("微信支付暂未开放", wechatResult.getMessage());

        assertFalse(alipayResult.getSuccess());
        assertEquals("501", alipayResult.getCode());
        assertEquals("支付宝支付暂未开放", alipayResult.getMessage());

        verifyNoInteractions(paymentService, userCouponService);
        verify(orderService).getById("missing-order");
    }

    @Test
    public void orderControllerRejectsThirdPartyBeforeCreatingPaymentRecord() {
        OrderService orderService = mock(OrderService.class);
        PaymentService paymentService = mock(PaymentService.class);
        WalletService walletService = mock(WalletService.class);
        OrderDishService orderDishService = mock(OrderDishService.class);
        PaymentPasswordService paymentPasswordService = mock(PaymentPasswordService.class);
        OrderController controller = new OrderController(
                orderService, paymentService, walletService, orderDishService, paymentPasswordService);

        Order order = new Order();
        order.setId("order-1");
        order.setUserId("user-1");
        order.setStatus(0);
        when(orderService.getById("order-1")).thenReturn(order);

        ResponseResult<?> result = controller.payOrder("order-1", "user-1", "wechat", "123456");

        assertFalse(result.getSuccess());
        assertEquals("400", result.getCode());
        assertEquals("仅支持平台币支付", result.getMessage());
        verifyNoInteractions(paymentService, walletService);
    }

    @Test
    public void groupOrderRejectsThirdPartyBeforeCreatingPaymentRecord() {
        GroupOrderService groupOrderService = mock(GroupOrderService.class);
        PaymentService paymentService = mock(PaymentService.class);
        UserCouponService userCouponService = mock(UserCouponService.class);
        WalletService walletService = mock(WalletService.class);

        GroupOrder groupOrder = new GroupOrder();
        groupOrder.setId("group-order-1");
        groupOrder.setStatus(0);

        when(groupOrderService.getGroupOrderDetail("group-order-1")).thenReturn(groupOrder);
        when(groupOrderService.getGroupOrderRuntimeState(groupOrder, "user-1"))
                .thenReturn(Map.of("currentUserJoined", true, "locked", true));

        GroupOrderController controller = new GroupOrderController();
        ReflectionTestUtils.setField(controller, "groupOrderService", groupOrderService);
        ReflectionTestUtils.setField(controller, "paymentService", paymentService);
        ReflectionTestUtils.setField(controller, "userCouponService", userCouponService);
        ReflectionTestUtils.setField(controller, "walletService", walletService);

        ResponseResult<?> result = controller.payGroupOrder(
                "group-order-1",
                Map.of("userId", "user-1", "paymentMethod", "wechat")
        );

        assertFalse(result.getSuccess());
        assertEquals("501", result.getCode());
        assertEquals("微信支付暂未开放", result.getMessage());
        verifyNoInteractions(paymentService, userCouponService, walletService);
    }
}
