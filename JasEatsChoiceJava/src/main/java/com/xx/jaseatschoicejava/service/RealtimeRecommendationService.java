package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.RecommendationResultDTO;
import com.xx.jaseatschoicejava.entity.Dish;

import java.util.List;

/**
 * 实时推荐推送服务接口
 */
public interface RealtimeRecommendationService {

    /**
     * 向指定用户推送个性化推荐
     * @param userId 用户ID
     * @param recommendations 推荐列表
     */
    void pushPersonalizedRecommendations(String userId, List<RecommendationResultDTO> recommendations);

    /**
     * 向指定用户推送新菜品推荐
     * @param userId 用户ID
     * @param newDishes 新菜品列表
     */
    void pushNewDishRecommendations(String userId, List<Dish> newDishes);

    /**
     * 向所有在线用户推送推荐更新通知
     * @param message 更新消息
     */
    void broadcastRecommendationUpdate(String message);

    /**
     * 向用户推送实时推荐（基于当前上下文）
     * @param userId 用户ID
     * @param context 上下文信息（天气、时间等）
     */
    void pushRealtimeRecommendation(String userId, String context);

    /**
     * 定时向所有用户推送推荐（后台任务）
     */
    void scheduleRecommendationPush();
}
