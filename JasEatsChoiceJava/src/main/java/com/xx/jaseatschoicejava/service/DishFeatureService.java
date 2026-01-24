package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.DishFeature;
import com.xx.jaseatschoicejava.entity.DishSimilarity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品特征服务接口
 */
public interface DishFeatureService {

    /**
     * 获取菜品特征
     */
    DishFeature getDishFeature(String dishId);

    /**
     * 为菜品提取特征
     */
    DishFeature extractFeatures(Dish dish);

    /**
     * 批量提取菜品特征
     */
    void batchExtractFeatures(List<String> dishIds);

    /**
     * 计算两个菜品之间的相似度（基于内容）
     */
    double calculateContentSimilarity(String dishIdA, String dishIdB);

    /**
     * 计算两个菜品之间的相似度（协同过滤）
     */
    double calculateCollaborativeSimilarity(String dishIdA, String dishIdB);

    /**
     * 计算菜品热度分数
     */
    BigDecimal calculatePopularityScore(String dishId);

    /**
     * 批量更新菜品热度分数
     */
    void batchUpdatePopularityScores();

    /**
     * 更新菜品相似度矩阵
     */
    void updateSimilarityMatrix();

    /**
     * 获取相似菜品推荐
     */
    List<DishSimilarity> getSimilarDishes(String dishId, String similarityType, int limit);

    /**
     * 预计算所有菜品的相似度（后台任务）
     */
    void precomputeSimilarities();
}
