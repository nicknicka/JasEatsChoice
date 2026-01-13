package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.PaymentRecord;

import java.math.BigDecimal;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<PaymentRecord> {

    /**
     * 创建支付记录
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @param amount 支付金额
     * @param paymentMethod 支付方式
     * @return 支付记录
     */
    PaymentRecord createPayment(String orderId, String userId, String merchantId, BigDecimal amount, String paymentMethod);

    /**
     * 处理支付
     * @param paymentNo 支付流水号
     * @return 是否成功
     */
    boolean processPayment(String paymentNo);

    /**
     * 钱包支付
     * @param paymentNo 支付流水号
     * @return 是否成功
     */
    boolean walletPayment(String paymentNo);

    /**
     * 取消支付
     * @param paymentNo 支付流水号
     * @return 是否成功
     */
    boolean cancelPayment(String paymentNo);

    /**
     * 根据订单ID获取支付记录
     * @param orderId 订单ID
     * @return 支付记录
     */
    PaymentRecord getPaymentByOrderId(String orderId);

    /**
     * 根据支付流水号获取支付记录
     * @param paymentNo 支付流水号
     * @return 支付记录
     */
    PaymentRecord getPaymentByPaymentNo(String paymentNo);

    /**
     * 生成支付流水号
     * @return 流水号
     */
    String generatePaymentNo();
}
