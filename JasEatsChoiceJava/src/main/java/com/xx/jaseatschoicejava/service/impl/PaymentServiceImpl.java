package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.entity.Wallet;
import com.xx.jaseatschoicejava.mapper.PaymentRecordMapper;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentService {

    private final WalletService walletService;
    private final OrderService orderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord createPayment(String orderId, String userId, String merchantId, BigDecimal amount, String paymentMethod) {
        // 生成支付流水号
        String paymentNo = generatePaymentNo();

        // 创建支付记录
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setPaymentNo(paymentNo);
        paymentRecord.setOrderId(orderId);
        paymentRecord.setUserId(userId);
        paymentRecord.setMerchantId(merchantId);
        paymentRecord.setAmount(amount);
        paymentRecord.setPaymentMethod(paymentMethod);
        paymentRecord.setPaymentStatus("pending");
        paymentRecord.setRefundAmount(BigDecimal.ZERO);
        paymentRecord.setCreateTime(LocalDateTime.now());
        paymentRecord.setUpdateTime(LocalDateTime.now());

        save(paymentRecord);
        log.info("创建支付记录成功，流水号：{}，订单：{}，金额：{}", paymentNo, orderId, amount);
        return paymentRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processPayment(String paymentNo) {
        PaymentRecord paymentRecord = getPaymentByPaymentNo(paymentNo);
        if (paymentRecord == null) {
            throw new RuntimeException("支付记录不存在");
        }

        if (!"pending".equals(paymentRecord.getPaymentStatus())) {
            throw new RuntimeException("支付状态异常");
        }

        // 根据支付方式处理
        if ("wallet".equals(paymentRecord.getPaymentMethod())) {
            return walletPayment(paymentNo);
        }

        // 其他支付方式（微信、支付宝）的接入预留
        throw new RuntimeException("暂不支持该支付方式");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean walletPayment(String paymentNo) {
        PaymentRecord paymentRecord = getPaymentByPaymentNo(paymentNo);
        if (paymentRecord == null) {
            throw new RuntimeException("支付记录不存在");
        }

        if (!"pending".equals(paymentRecord.getPaymentStatus())) {
            throw new RuntimeException("支付状态异常");
        }

        try {
            // 扣减钱包余额
            String description = "支付订单 - " + paymentRecord.getOrderId();
            boolean success = walletService.deductBalance(
                paymentRecord.getUserId(),
                paymentRecord.getAmount(),
                description
            );

            if (!success) {
                throw new RuntimeException("扣费失败");
            }

            // 更新支付状态
            paymentRecord.setPaymentStatus("success");
            paymentRecord.setPaidTime(LocalDateTime.now());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            updateById(paymentRecord);

            // 更新订单状态
            Order order = orderService.getById(paymentRecord.getOrderId());
            if (order != null) {
                order.setStatus(1); // 待接单
                order.setPaymentId(paymentRecord.getId());
                order.setPaidAmount(paymentRecord.getAmount());
                order.setPaymentTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                orderService.updateById(order);
            }

            log.info("钱包支付成功，流水号：{}，金额：{}", paymentNo, paymentRecord.getAmount());
            return true;

        } catch (Exception e) {
            log.error("钱包支付失败，流水号：{}", paymentNo, e);
            paymentRecord.setPaymentStatus("failed");
            paymentRecord.setUpdateTime(LocalDateTime.now());
            updateById(paymentRecord);
            throw new RuntimeException("支付失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPayment(String paymentNo) {
        PaymentRecord paymentRecord = getPaymentByPaymentNo(paymentNo);
        if (paymentRecord == null) {
            throw new RuntimeException("支付记录不存在");
        }

        if (!"pending".equals(paymentRecord.getPaymentStatus())) {
            throw new RuntimeException("只能取消待支付的订单");
        }

        paymentRecord.setPaymentStatus("failed");
        paymentRecord.setUpdateTime(LocalDateTime.now());
        updateById(paymentRecord);

        log.info("取消支付成功，流水号：{}", paymentNo);
        return true;
    }

    @Override
    public PaymentRecord getPaymentByOrderId(String orderId) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getOrderId, orderId);
        queryWrapper.orderByDesc(PaymentRecord::getCreateTime);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    public PaymentRecord getPaymentByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentRecord::getPaymentNo, paymentNo);
        return getOne(queryWrapper);
    }

    @Override
    public String generatePaymentNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "PAY" + timestamp + (int)(Math.random() * 1000);
    }
}
