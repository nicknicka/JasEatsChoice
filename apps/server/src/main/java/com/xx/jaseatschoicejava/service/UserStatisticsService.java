package com.xx.jaseatschoicejava.service;

import java.util.Map;

/**
 * 用户统计服务接口
 */
public interface UserStatisticsService {

    /**
     * 获取用户消费统计
     * @param userId 用户ID
     * @param days 统计天数
     * @return 消费统计数据
     */
    Map<String, Object> getConsumeStatistics(String userId, Integer days);

    /**
     * 获取用户卡路里摄入统计
     * @param userId 用户ID
     * @param days 统计天数
     * @return 卡路里摄入统计数据
     */
    Map<String, Object> getCaloriesStatistics(String userId, Integer days);

    /**
     * 获取用户饮食记录统计
     * @param userId 用户ID
     * @param days 统计天数
     * @return 饮食记录统计数据
     */
    Map<String, Object> getDietRecordsStatistics(String userId, Integer days);

    /**
     * 获取用户收藏菜品统计
     * @param userId 用户ID
     * @return 收藏菜品统计数据
     */
    Map<String, Object> getFavoritesStatistics(String userId);

    /**
     * 获取用户综合统计概览
     * @param userId 用户ID
     * @return 综合统计数据
     */
    Map<String, Object> getOverviewStatistics(String userId);
}
