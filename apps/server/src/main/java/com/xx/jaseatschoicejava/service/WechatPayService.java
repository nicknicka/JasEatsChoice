package com.xx.jaseatschoicejava.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 微信支付服务接口
 */
public interface WechatPayService {

    /**
     * 创建微信支付订单（小程序支付）
     * @param paymentNo 支付流水号
     * @param amount 支付金额
     * @param description 商品描述
     * @param openid 用户openid
     * @return 支付参数（包含timeStamp, nonceStr, package, signType, paySign）
     */
    Map<String, Object> createMiniPayOrder(String paymentNo, BigDecimal amount, String description, String openid);

    /**
     * 创建微信支付订单（H5支付）
     * @param paymentNo 支付流水号
     * @param amount 支付金额
     * @param description 商品描述
     * @return 支付URL
     */
    String createH5PayOrder(String paymentNo, BigDecimal amount, String description);

    /**
     * 查询微信支付订单
     * @param outTradeNo 商户订单号（支付流水号）
     * @return 支付状态
     */
    Map<String, Object> queryOrder(String outTradeNo);

    /**
     * 关闭微信支付订单
     * @param outTradeNo 商户订单号
     * @return 是否成功
     */
    boolean closeOrder(String outTradeNo);

    /**
     * 微信支付回调处理
     * @param notification 回调数据
     * @return 处理结果
     */
    Map<String, Object> handleNotification(Map<String, Object> notification);

    /**
     * 申请退款
     * @param paymentNo 支付流水号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款结果
     */
    Map<String, Object> refund(String paymentNo, BigDecimal refundAmount, String reason);

    /**
     * 查询退款
     * @param outTradeNo 商户订单号
     * @return 退款状态
     */
    Map<String, Object> queryRefund(String outTradeNo);

    /**
     * 验证签名
     * @param data 待验证数据
     * @param signature 签名
     * @return 是否验证通过
     */
    boolean verifySignature(Map<String, Object> data, String signature);
}
