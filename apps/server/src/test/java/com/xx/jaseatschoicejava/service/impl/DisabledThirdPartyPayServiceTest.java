package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AlipayPayService;
import com.xx.jaseatschoicejava.service.WechatPayService;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DisabledThirdPartyPayServiceTest {

    private final WechatPayService wechatPayService = new WechatPayServiceImpl();
    private final AlipayPayService alipayPayService = new AlipayPayServiceImpl();

    @Test
    public void wechatPayMethodsAreExplicitlyDisabled() {
        assertDisabled("微信支付暂未开放", () -> wechatPayService.createMiniPayOrder("P1", BigDecimal.ONE, "订单", "openid"));
        assertDisabled("微信支付暂未开放", () -> wechatPayService.createH5PayOrder("P1", BigDecimal.ONE, "订单"));
        assertDisabled("微信支付暂未开放", () -> wechatPayService.queryOrder("P1"));
        assertDisabled("微信支付暂未开放", () -> wechatPayService.closeOrder("P1"));
        assertDisabled("微信支付暂未开放", () -> wechatPayService.handleNotification(Map.of()));
        assertDisabled("微信支付暂未开放", () -> wechatPayService.refund("P1", BigDecimal.ONE, "原因"));
        assertDisabled("微信支付暂未开放", () -> wechatPayService.queryRefund("P1"));
        assertDisabled("微信支付暂未开放", () -> wechatPayService.verifySignature(Map.of(), "signature"));
    }

    @Test
    public void alipayPayMethodsAreExplicitlyDisabled() {
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.createWapPayOrder("P1", BigDecimal.ONE, "订单"));
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.createAppPayOrder("P1", BigDecimal.ONE, "订单"));
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.queryOrder("P1"));
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.closeOrder("P1"));
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.handleNotification(Map.of()));
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.refund("P1", BigDecimal.ONE, "原因"));
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.queryRefund("P1"));
        assertDisabled("支付宝支付暂未开放", () -> alipayPayService.verifySignature(Map.of(), "signature"));
    }

    private void assertDisabled(String expectedMessage, org.junit.function.ThrowingRunnable executable) {
        BusinessException exception = assertThrows(BusinessException.class, executable);
        assertEquals("501", exception.getCode());
        assertEquals(expectedMessage, exception.getMessage());
    }
}
