package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;

import java.math.BigDecimal;

/**
 * 提现记录Service接口
 */
public interface WithdrawRecordService extends IService<WithdrawRecord> {

    /**
     * 分页查询提现记录
     */
    IPage<WithdrawRecord> getWithdrawPage(Page<WithdrawRecord> pageParam, String keyword, String status);

    /**
     * 获取提现详情
     */
    WithdrawRecord getWithdrawDetail(String withdrawId);

    /**
     * 处理提现审核
     */
    boolean processWithdraw(String withdrawId, String decision, String comment, Long adminId);

    /**
     * 汇总指定状态的提现金额
     */
    BigDecimal sumWithdrawAmountByStatus(String status);

    /**
     * 创建提现申请记录
     * @param userId 用户ID
     * @param amount 提现金额
     * @param withdrawNo 提现流水号
     * @param withdrawMethod 提现方式
     * @param accountInfo 提现账号信息
     * @return 创建的提现记录
     */
    WithdrawRecord createWithdrawRequest(String userId, BigDecimal amount, String withdrawNo, String withdrawMethod, String accountInfo);

    /**
     * 执行提现扣款（审核通过后调用）
     * @param withdrawId 提现记录ID
     * @return 是否成功
     */
    boolean executeWithdrawDeduction(String withdrawId);
}
