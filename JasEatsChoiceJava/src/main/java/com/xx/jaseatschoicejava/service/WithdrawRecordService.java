package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;

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
    java.math.BigDecimal sumWithdrawAmountByStatus(String status);
}
