package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.RechargeRecord;

/**
 * 充值记录服务接口
 */
public interface RechargeRecordService extends IService<RechargeRecord> {

    /**
     * 分页查询充值记录
     */
    IPage<RechargeRecord> getRechargePage(Page<RechargeRecord> page, String keyword, String paymentMethod, String status);

    /**
     * 获取充值记录详情
     */
    RechargeRecord getRechargeDetail(String rechargeId);
}
