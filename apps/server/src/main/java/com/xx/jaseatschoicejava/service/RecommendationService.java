package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.RecommendationRequestDTO;
import com.xx.jaseatschoicejava.dto.RecommendationResultDTO;
import com.xx.jaseatschoicejava.entity.Dish;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐服务接口
 */
public interface RecommendationService {

    /**
     * 获取推荐菜品（主接口）
     * 整合多种召回策略和排序策略
     */
    List<RecommendationResultDTO> getRecommendations(RecommendationRequestDTO request);

    /**
     * 记录推荐反馈（点击、下单等）
     */
    void recordFeedback(String userId, String dishId, String recommendationId, Boolean isClicked, Boolean isOrdered);

    /**
     * 刷新推荐（用户主动刷新）
     */
    List<RecommendationResultDTO> refreshRecommendations(String userId);

    /**
     * 替换推荐菜品
     */
    List<Dish> replaceRecommendDishes(String userId, List<String> dishIds);

    /**
     * 筛选推荐菜品
     */
    List<Dish> filterRecommendDishes(String userId, String category, Integer minCalorie, Integer maxCalorie, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 获取推荐理由
     */
    String getRecommendationReason(String dishId, String userId);
}
