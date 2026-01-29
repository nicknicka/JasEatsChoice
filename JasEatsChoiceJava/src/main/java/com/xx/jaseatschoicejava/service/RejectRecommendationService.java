package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.entity.RejectRecommendation;

import java.util.List;

/**
 * 推荐拒绝Service接口
 */
public interface RejectRecommendationService {

    /**
     * 记录用户拒绝推荐
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @param reason 拒绝原因（可选）
     * @return 拒绝记录
     */
    RejectRecommendation addRejectRecord(String userId, String dishId, String reason);

    /**
     * 统计用户对某个菜品的拒绝次数
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @return 拒绝次数
     */
    int countRejects(String userId, String dishId);

    /**
     * 查询用户所有被拒绝的菜品ID列表
     * @param userId 用户ID
     * @return 菜品ID列表
     */
    List<String> getRejectedDishIds(String userId);

    /**
     * 查询用户被拒绝超过指定次数的菜品ID列表
     * @param userId 用户ID
     * @param threshold 拒绝次数阈值
     * @return 菜品ID列表
     */
    List<String> getFrequentlyRejectedDishIds(String userId, int threshold);

    /**
     * 清除用户对某个菜品的拒绝记录
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @return 是否成功
     */
    boolean clearRejectRecord(String userId, String dishId);
}
