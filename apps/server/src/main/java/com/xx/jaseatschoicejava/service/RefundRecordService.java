package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.RefundRecord;

import java.math.BigDecimal;

/**
 * 退款记录服务接口
 */
public interface RefundRecordService extends IService<RefundRecord> {

    /**
     * 分页查询退款记录列表
     */
    IPage<RefundRecord> getRefundPage(Page<RefundRecord> page, String keyword, String status);

    /**
     * 获取退款记录详情
     */
    RefundRecord getRefundDetail(String refundId);

    /**
     * 创建退款记录
     */
    boolean createRefund(RefundRecord refundRecord);

    /**
     * 处理退款申请
     */
    boolean processRefund(String refundId, String decision, String comment, Long auditBy);

    /**
     * 统计退款金额（按状态）
     */
    BigDecimal sumRefundAmountByStatus(String status);

    /**
     * 生成退款流水号
     */
    String generateRefundNo();
}
