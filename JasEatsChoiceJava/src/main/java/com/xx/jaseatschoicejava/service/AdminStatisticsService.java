package com.xx.jaseatschoicejava.service;

import java.util.Map;

/**
 * 管理员统计Service接口
 */
public interface AdminStatisticsService {

    /**
     * 获取仪表板统计数据
     * @param days 统计天数（默认7天）
     * @return 统计数据
     */
    Map<String, Object> getDashboardStatistics(int days);
}
