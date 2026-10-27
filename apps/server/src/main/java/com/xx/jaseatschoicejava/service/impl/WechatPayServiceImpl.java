package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.WechatPayService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 微信支付服务实现。
 *
 * <p>当前项目无微信支付权限，且不接入第三方支付 SDK，所有支付能力均未启用。
 * 本类保留接口骨架，调用时必须明确失败，不能返回伪造的支付参数或成功结果。</p>
 */
@Service
public class WechatPayServiceImpl implements WechatPayService {

    private static final String DISABLED_CODE = "501";
    private static final String DISABLED_MESSAGE = "微信支付暂未开放";

    @Override
    public Map<String, Object> createMiniPayOrder(String paymentNo, BigDecimal amount, String description, String openid) {
        throw disabled();
    }

    @Override
    public String createH5PayOrder(String paymentNo, BigDecimal amount, String description) {
        throw disabled();
    }

    @Override
    public Map<String, Object> queryOrder(String outTradeNo) {
        throw disabled();
    }

    @Override
    public boolean closeOrder(String outTradeNo) {
        throw disabled();
    }

    @Override
    public Map<String, Object> handleNotification(Map<String, Object> notification) {
        throw disabled();
    }

    @Override
    public Map<String, Object> refund(String paymentNo, BigDecimal refundAmount, String reason) {
        throw disabled();
    }

    @Override
    public Map<String, Object> queryRefund(String outTradeNo) {
        throw disabled();
    }

    @Override
    public boolean verifySignature(Map<String, Object> data, String signature) {
        throw disabled();
    }

    private BusinessException disabled() {
        return new BusinessException(DISABLED_CODE, DISABLED_MESSAGE);
    }
}
