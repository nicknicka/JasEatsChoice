package com.xx.jaseatschoicejava.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.service.PaymentService;
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

            // 查询超时的待支付记录
            LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PaymentRecord::getPaymentStatus, "pending")
                    .le(PaymentRecord::getCreateTime, timeoutTime);

            List<PaymentRecord> expiredPayments = paymentService.list(queryWrapper);

            if (expiredPayments.isEmpty()) {
                log.info("没有超时支付需要处理");
                return;
            }

            log.info("发现{}笔超时支付，开始处理...", expiredPayments.size());

            // 批量更新超时支付状态为失败
            int successCount = 0;
            for (PaymentRecord payment : expiredPayments) {
                try {
                    LambdaUpdateWrapper<PaymentRecord> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(PaymentRecord::getId, payment.getId())
                            .eq(PaymentRecord::getPaymentStatus, "pending") // 乐观锁，确保状态未被修改
                            .set(PaymentRecord::getPaymentStatus, "failed")
                            .set(PaymentRecord::getRemark, "支付超时自动取消")
                            .set(PaymentRecord::getUpdateTime, LocalDateTime.now());

                    boolean updated = paymentService.update(updateWrapper);
                    if (updated) {
                        successCount++;
                        log.info("超时支付已自动取消，流水号：{}", payment.getPaymentNo());
                    }
                } catch (Exception e) {
                    log.error("取消超时支付失败，流水号：{}", payment.getPaymentNo(), e);
                }
            }

            log.info("超时支付处理完成，成功：{}，失败：{}", successCount, expiredPayments.size() - successCount);

        } catch (Exception e) {
            log.error("检查超时支付失败", e);
        }
    }
}
