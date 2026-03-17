package com.xx.jaseatschoicejava.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付超时检查定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTimeoutScheduler {

    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * 支付超时时间（分钟）
     * 默认15分钟
     */
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    /**
     * 定时检查超时支付
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkExpiredPayments() {
        try {
            log.info("开始检查超时支付...");

            // 计算超时时间点
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);

            // 第一部分：处理有支付记录的超时订单
            // 查询超时的待支付记录
            LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PaymentRecord::getPaymentStatus, "pending")
                    .le(PaymentRecord::getCreateTime, timeoutTime);

            List<PaymentRecord> expiredPayments = paymentService.list(queryWrapper);

            int paymentSuccessCount = 0;
            int orderSuccessCount = 0;

            if (!expiredPayments.isEmpty()) {
                log.info("发现{}笔超时支付（有支付记录），开始处理...", expiredPayments.size());

                for (PaymentRecord payment : expiredPayments) {
                    try {
                        // 1. 更新支付记录状态为失败
                        LambdaUpdateWrapper<PaymentRecord> paymentUpdateWrapper = new LambdaUpdateWrapper<>();
                        paymentUpdateWrapper.eq(PaymentRecord::getId, payment.getId())
                                .eq(PaymentRecord::getPaymentStatus, "pending") // 乐观锁，确保状态未被修改
                                .set(PaymentRecord::getPaymentStatus, "failed")
                                .set(PaymentRecord::getRemark, "支付超时自动取消")
                                .set(PaymentRecord::getUpdateTime, LocalDateTime.now());

                        boolean paymentUpdated = paymentService.update(paymentUpdateWrapper);

                        if (paymentUpdated) {
                            paymentSuccessCount++;
                            log.info("超时支付已自动取消，流水号：{}", payment.getPaymentNo());

                            // 2. 取消对应的订单
                            if (payment.getOrderId() != null) {
                                Order order = orderService.getById(payment.getOrderId());
                                if (order != null && order.getStatus() == 0) { // 只有待支付订单才能自动取消
                                    order.setStatus(4); // 4-已取消
                                    order.setUpdateTime(LocalDateTime.now());
                                    boolean orderUpdated = orderService.updateById(order);

                                    if (orderUpdated) {
                                        orderSuccessCount++;
                                        log.info("超时订单已自动取消，订单ID：{}", payment.getOrderId());

                                        // 3. 通知用户订单已取消
                                        try {
                                            NotificationUtil.createOrderNotification(
                                                order.getUserId(),
                                                NotificationTypeEnum.ORDER_CANCELLED,
                                                payment.getOrderId(),
                                                "支付超时自动取消"
                                            );
                                        } catch (Exception notifyEx) {
                                            log.warn("发送订单取消通知失败，订单ID：{}", payment.getOrderId(), notifyEx);
                                        }
                                    } else {
                                        log.warn("更新订单状态失败，订单ID：{}", payment.getOrderId());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("取消超时支付失败，流水号：{}", payment.getPaymentNo(), e);
                    }
                }
            }

            // 第二部分：处理没有支付记录的超时订单
            LambdaQueryWrapper<Order> orderQueryWrapper = new LambdaQueryWrapper<>();
            orderQueryWrapper.eq(Order::getStatus, 0) // 待支付
                    .isNull(Order::getPaymentId) // 没有支付记录
                    .le(Order::getCreateTime, timeoutTime);

            List<Order> expiredOrdersWithoutPayment = orderService.list(orderQueryWrapper);

            if (!expiredOrdersWithoutPayment.isEmpty()) {
                log.info("发现{}笔超时订单（无支付记录），开始处理...", expiredOrdersWithoutPayment.size());

                for (Order order : expiredOrdersWithoutPayment) {
                    try {
                        order.setStatus(4); // 4-已取消
                        order.setUpdateTime(LocalDateTime.now());
                        boolean orderUpdated = orderService.updateById(order);

                        if (orderUpdated) {
                            orderSuccessCount++;
                            log.info("超时订单已自动取消（无支付记录），订单ID：{}", order.getId());

                            // 通知用户订单已取消
                            try {
                                NotificationUtil.createOrderNotification(
                                    order.getUserId(),
                                    NotificationTypeEnum.ORDER_CANCELLED,
                                    order.getId(),
                                    "支付超时自动取消"
                                );
                            } catch (Exception notifyEx) {
                                log.warn("发送订单取消通知失败，订单ID：{}", order.getId(), notifyEx);
                            }
                        } else {
                            log.warn("更新订单状态失败，订单ID：{}", order.getId());
                        }
                    } catch (Exception e) {
                        log.error("取消超时订单失败，订单ID：{}", order.getId(), e);
                    }
                }
            }

            if (expiredPayments.isEmpty() && expiredOrdersWithoutPayment.isEmpty()) {
                log.info("没有超时订单需要处理");
            } else {
                log.info("超时订单处理完成，支付记录更新：{}，订单取消成功：{}",
                        paymentSuccessCount, orderSuccessCount);
            }

        } catch (Exception e) {
            log.error("检查超时支付失败", e);
        }
    }
}
