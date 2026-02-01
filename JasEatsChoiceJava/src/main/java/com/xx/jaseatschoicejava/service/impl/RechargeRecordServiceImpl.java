package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.RechargeRecord;
import com.xx.jaseatschoicejava.mapper.RechargeRecordMapper;
import com.xx.jaseatschoicejava.service.RechargeRecordService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

/**
 * 充值记录服务实现
 */
@Service
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordMapper, RechargeRecord>
        implements RechargeRecordService {

    @Override
    public IPage<RechargeRecord> getRechargePage(Page<RechargeRecord> page, String keyword,
                                                 String paymentMethod, String status) {
        // 基础查询
        return baseMapper.selectPage(page,
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RechargeRecord>()
                .like(keyword != null, "recharge_no", keyword)
                .eq(paymentMethod != null, "payment_method", paymentMethod)
                .eq(status != null, "recharge_status", status)
                .orderByDesc("create_time")
        );
    }

    @Override
    public RechargeRecord getRechargeDetail(String rechargeId) {
        return baseMapper.selectById(rechargeId);
    }
}
