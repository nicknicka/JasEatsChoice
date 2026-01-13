package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.ConsumeHistory;

public interface ConsumeHistoryService extends IService<ConsumeHistory> {

    /**
     * 根据用户ID获取消费记录分页
     * @param userId 用户ID
     * @param type 类型（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Page<ConsumeHistory> getConsumeHistoryByUserId(String userId, String type, String startDate, String endDate, Integer pageNum, Integer pageSize);
}
