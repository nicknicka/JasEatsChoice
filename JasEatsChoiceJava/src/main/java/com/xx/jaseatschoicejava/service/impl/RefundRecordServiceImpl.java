package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.RefundRecord;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.mapper.RefundRecordMapper;
import com.xx.jaseatschoicejava.service.RefundRecordService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 退款记录服务实现
 */
@Service
public class RefundRecordServiceImpl extends ServiceImpl<RefundRecordMapper, RefundRecord>
        implements RefundRecordService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private WalletService walletService;

    @Override
    public IPage<RefundRecord> getRefundPage(Page<RefundRecord> page, String keyword, String status) {
        return baseMapper.selectRefundPageWithUser(page, keyword, status);
    }

    @Override
    public RefundRecord getRefundDetail(String refundId) {
        return baseMapper.selectById(refundId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRefund(RefundRecord refundRecord) {
        // 生成退款流水号
        refundRecord.setRefundNo(generateRefundNo());
        refundRecord.setStatus("PENDING");
        refundRecord.setApplyTime(LocalDateTime.now());
        return save(refundRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRefund(String refundId, String decision, String comment, Long auditBy) {
        RefundRecord refundRecord = getById(refundId);
        if (refundRecord == null) {
            throw new RuntimeException("退款记录不存在");
        }

        // 检查状态
        if (!"PENDING".equals(refundRecord.getStatus())) {
            throw new RuntimeException("该退款申请已处理，无法重复操作");
        }

        if ("APPROVE".equals(decision)) {
            // 审核通过
            refundRecord.setStatus("PROCESSING");
            refundRecord.setProcessTime(LocalDateTime.now());
            refundRecord.setAuditBy(auditBy);
            refundRecord.setProcessComment(comment);

            // 更新退款记录
            updateById(refundRecord);

            // TODO: 调用第三方退款接口（如微信、支付宝）
            // 这里简化处理，直接退款到余额
            try {
                // 退款到用户余额
                walletService.refund(
                    String.valueOf(refundRecord.getUserId()),
                    refundRecord.getRefundAmount(),
                    "订单退款：" + refundRecord.getOrderNo()
                );

                // 更新退款状态为成功
                refundRecord.setStatus("SUCCESS");
                refundRecord.setRefundMethod("BALANCE");
                refundRecord.setCompleteTime(LocalDateTime.now());
                updateById(refundRecord);

                // 更新订单状态
                Order order = orderService.getById(String.valueOf(refundRecord.getOrderId()));
                if (order != null) {
                    order.setStatus(7); // 7-已退款
                    orderService.updateById(order);
                }

            } catch (Exception e) {
                // 退款失败
                refundRecord.setStatus("FAILED");
                refundRecord.setProcessComment("退款失败：" + e.getMessage());
                updateById(refundRecord);
                throw new RuntimeException("退款处理失败：" + e.getMessage());
            }

        } else if ("REJECT".equals(decision)) {
            // 审核拒绝
            refundRecord.setStatus("REJECTED");
            refundRecord.setProcessTime(LocalDateTime.now());
            refundRecord.setAuditBy(auditBy);
            refundRecord.setProcessComment(comment);
            updateById(refundRecord);
        }

        return true;
    }

    @Override
    public BigDecimal sumRefundAmountByStatus(String status) {
        return baseMapper.sumRefundAmountByStatus(status);
    }

    @Override
    public String generateRefundNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((long) (Math.random() * 10000));
        return "RF" + timestamp + String.format("%04d", Long.parseLong(random));
    }
}
