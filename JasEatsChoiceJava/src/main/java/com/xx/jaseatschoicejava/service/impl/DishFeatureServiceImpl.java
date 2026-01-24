package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.DishFeature;
import com.xx.jaseatschoicejava.entity.DishSimilarity;
import com.xx.jaseatschoicejava.mapper.DishFeatureMapper;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.mapper.DishSimilarityMapper;
import com.xx.jaseatschoicejava.service.DishFeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜品特征服务实现
 */
@Slf4j
@Service
public class DishFeatureServiceImpl implements DishFeatureService {

    @Autowired
    private DishFeatureMapper dishFeatureMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishSimilarityMapper dishSimilarityMapper;

    @Override
    public DishFeature getDishFeature(String dishId) {
        DishFeature feature = dishFeatureMapper.getByDishId(dishId);

        if (feature == null) {
            // 如果特征不存在，尝试提取
            Dish dish = dishMapper.selectById(dishId);
            if (dish != null) {
                feature = extractFeatures(dish);
                dishFeatureMapper.insert(feature);
            }
        }

        return feature;
    }

    @Override
    public DishFeature extractFeatures(Dish dish) {
        if (dish == null) {
            return null;
        }

        DishFeature feature = new DishFeature();
        feature.setDishId(String.valueOf(dish.getId()));
        feature.setCategory(dish.getCategory());

        // 提取标签（从description或其他字段）
        List<String> tags = extractTags(dish);
        feature.setTags(tags);

        // 提取风味特征
        Map<String, Double> flavorProfile = extractFlavorProfile(dish, tags);
        feature.setFlavorProfile(flavorProfile);

        // 提取营养信息
        DishFeature.NutritionInfo nutritionInfo = extractNutritionInfo(dish);
        feature.setNutritionInfo(nutritionInfo);

        // 提取烹饪方式
        feature.setCookingMethod(extractCookingMethod(dish));

        // 提取适用场景
        List<String> scenarios = extractSuitableScenarios(dish);
        feature.setSuitableScenarios(scenarios);

        // 提取时段标签
        List<String> timePeriodTags = extractTimePeriodTags(dish);
        feature.setTimePeriodTags(timePeriodTags);

        // 提取季节标签
        List<String> seasonTags = extractSeasonTags(dish);
        feature.setSeasonTags(seasonTags);

        // 计算价格等级
        Integer priceLevel = calculatePriceLevel(dish.getPrice());
        feature.setPriceLevel(priceLevel);

        // 计算热度分数
        BigDecimal popularityScore = calculatePopularityScore(String.valueOf(dish.getId()));
        feature.setPopularityScore(popularityScore);

        feature.setCreatedTime(LocalDateTime.now());
        feature.setUpdatedTime(LocalDateTime.now());

        return feature;
    }

    @Override
    @Transactional
    public void batchExtractFeatures(List<String> dishIds) {
        log.info("开始批量提取菜品特征，菜品数量：{}", dishIds.size());

        int successCount = 0;
        int failCount = 0;

        for (String dishId : dishIds) {
            try {
                Dish dish = dishMapper.selectById(dishId);
                if (dish != null) {
                    DishFeature feature = extractFeatures(dish);

                    // 使用insert或update
                    DishFeature existing = dishFeatureMapper.getByDishId(dishId);
                    if (existing == null) {
                        dishFeatureMapper.insert(feature);
                    } else {
                        dishFeatureMapper.updateById(feature);
                    }

                    successCount++;
                }
            } catch (Exception e) {
                log.error("提取菜品特征失败：dishId={}", dishId, e);
                failCount++;
            }
        }

        log.info("批量提取菜品特征完成：成功={}, 失败={}", successCount, failCount);
    }

    @Override
    public double calculateContentSimilarity(String dishIdA, String dishIdB) {
        DishFeature featureA = getDishFeature(dishIdA);
        DishFeature featureB = getDishFeature(dishIdB);

        if (featureA == null || featureB == null) {
            return 0.0;
        }

        double similarity = 0.0;
        double totalWeight = 0.0;

        // 1. 类别相似度（权重: 0.3）
        double categoryScore = featureA.getCategory() != null && featureA.getCategory().equals(featureB.getCategory()) ? 1.0 : 0.0;
        similarity += categoryScore * 0.3;
        totalWeight += 0.3;

        // 2. 标签相似度（权重: 0.25）
        double tagScore = calculateTagSimilarity(featureA.getTags(), featureB.getTags());
        similarity += tagScore * 0.25;
        totalWeight += 0.25;

        // 3. 风味相似度（权重: 0.25）
        double flavorScore = calculateFlavorSimilarity(featureA.getFlavorProfile(), featureB.getFlavorProfile());
        similarity += flavorScore * 0.25;
        totalWeight += 0.25;

        // 4. 价格等级相似度（权重: 0.1）
        double priceScore = calculatePriceSimilarity(featureA.getPriceLevel(), featureB.getPriceLevel());
        similarity += priceScore * 0.1;
        totalWeight += 0.1;

        // 5. 营养相似度（权重: 0.1）
        double nutritionScore = calculateNutritionSimilarity(featureA.getNutritionInfo(), featureB.getNutritionInfo());
        similarity += nutritionScore * 0.1;
        totalWeight += 0.1;

        return totalWeight > 0 ? similarity / totalWeight : 0.0;
    }

    @Override
    public double calculateCollaborativeSimilarity(String dishIdA, String dishIdB) {
        // 简化实现：基于共同用户行为的相似度
        // 实际应该使用更复杂的协同过滤算法（如矩阵分解）

        // 未来可以从user_behavior表统计两个菜品的共同用户
        // 相似度 = 共同用户数 / sqrt(用户A数 * 用户B数)

        return 0.5; // 暂时返回固定值
    }

    @Override
    public BigDecimal calculatePopularityScore(String dishId) {
        try {
            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) {
                return BigDecimal.ZERO;
            }

            // 基于多个维度计算热度（0-100）
            double popularity = 0.0;

            // 1. 浏览次数（权重30%）
            // TODO: 从统计表获取，这里使用基础分数
            popularity += 0.3 * 30;

            // 2. 下单次数（权重50%）
            // TODO: 从订单表统计
            popularity += 0.5 * 50;

            // 3. 收藏次数（权重20%）
            // TODO: 从收藏表统计
            popularity += 0.3 * 20;

            return BigDecimal.valueOf(popularity).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算菜品热度失败：dishId={}", dishId, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    @Transactional
    public void batchUpdatePopularityScores() {
        log.info("开始批量更新菜品热度分数");

        try {
            List<Dish> allDishes = dishMapper.selectList(null);

            for (Dish dish : allDishes) {
                BigDecimal score = calculatePopularityScore(String.valueOf(dish.getId()));

                DishFeature feature = dishFeatureMapper.getByDishId(String.valueOf(dish.getId()));
                if (feature != null) {
                    feature.setPopularityScore(score);
                    feature.setUpdatedTime(LocalDateTime.now());
                    dishFeatureMapper.updateById(feature);
                }
            }

            log.info("批量更新菜品热度分数完成，处理菜品数：{}", allDishes.size());
        } catch (Exception e) {
            log.error("批量更新菜品热度分数失败", e);
        }
    }

    @Override
    @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4点执行
    @Transactional
    public void updateSimilarityMatrix() {
        log.info("开始更新菜品相似度矩阵");

        try {
            List<Dish> allDishes = dishMapper.selectList(null);
            List<String> dishIds = allDishes.stream()
                    .map(d -> String.valueOf(d.getId()))
                    .collect(Collectors.toList());

            int totalPairs = dishIds.size() * (dishIds.size() - 1) / 2;
            int processedPairs = 0;

            // 计算所有菜品对的相似度（只计算上三角矩阵）
            for (int i = 0; i < dishIds.size(); i++) {
                for (int j = i + 1; j < dishIds.size(); j++) {
                    String dishIdA = dishIds.get(i);
                    String dishIdB = dishIds.get(j);

                    try {
                        // 计算内容相似度
                        double contentSim = calculateContentSimilarity(dishIdA, dishIdB);

                        // 保存相似度（如果大于阈值）
                        if (contentSim > 0.3) {
                            saveSimilarity(dishIdA, dishIdB, contentSim, DishSimilarity.SimilarityType.CONTENT.getCode());
                            saveSimilarity(dishIdB, dishIdA, contentSim, DishSimilarity.SimilarityType.CONTENT.getCode());
                        }

                        processedPairs++;

                        // 每100对输出一次进度
                        if (processedPairs % 100 == 0) {
                            log.info("相似度计算进度：{}/{}", processedPairs, totalPairs);
                        }
                    } catch (Exception e) {
                        log.error("计算相似度失败：{} vs {}", dishIdA, dishIdB, e);
                    }
                }
            }

            log.info("菜品相似度矩阵更新完成");
        } catch (Exception e) {
            log.error("更新菜品相似度矩阵失败", e);
        }
    }

    @Override
    public List<DishSimilarity> getSimilarDishes(String dishId, String similarityType, int limit) {
        return dishSimilarityMapper.getSimilarDishes(dishId, similarityType, limit);
    }

    @Override
    @Transactional
    public void precomputeSimilarities() {
        log.info("开始预计算菜品相似度");
        updateSimilarityMatrix();
    }

    /**
     * 提取菜品标签
     */
    private List<String> extractTags(Dish dish) {
        List<String> tags = new ArrayList<>();

        // 从分类提取
        if (dish.getCategory() != null) {
            tags.add(dish.getCategory());
        }

        // 从描述提取关键词
        if (dish.getDescription() != null) {
            String desc = dish.getDescription().toLowerCase();

            // 辣味
            if (desc.contains("辣") || desc.contains("麻辣") || desc.contains("香辣")) {
                tags.add("辣");
            }
            // 清淡
            if (desc.contains("清淡") || desc.contains("清爽")) {
                tags.add("清淡");
            }
            // 下饭
            if (desc.contains("下饭") || desc.contains("米饭")) {
                tags.add("下饭");
            }
            // 汤
            if (desc.contains("汤") || desc.contains("煲")) {
                tags.add("汤类");
            }
        }

        return tags;
    }

    /**
     * 提取风味特征
     */
    private Map<String, Double> extractFlavorProfile(Dish dish, List<String> tags) {
        Map<String, Double> flavor = new HashMap<>();

        if (tags == null) {
            return flavor;
        }

        // 基于标签判断风味
        for (String tag : tags) {
            if (tag.contains("辣")) {
                flavor.put("spicy", 0.8);
            }
            if (tag.contains("甜") || tag.contains("糖")) {
                flavor.put("sweet", 0.7);
            }
            if (tag.contains("咸") || tag.contains("盐")) {
                flavor.put("salty", 0.6);
            }
            if (tag.contains("酸")) {
                flavor.put("sour", 0.7);
            }
        }

        // 默认值（如果没有明确风味）
        if (flavor.isEmpty()) {
            flavor.put("spicy", 0.3);
            flavor.put("salty", 0.3);
        }

        return flavor;
    }

    /**
     * 提取营养信息
     */
    private DishFeature.NutritionInfo extractNutritionInfo(Dish dish) {
        DishFeature.NutritionInfo nutrition = new DishFeature.NutritionInfo();

        // 如果Dish实体有卡路里字段
        if (dish.getCalorie() != null) {
            nutrition.setCalories(dish.getCalorie().doubleValue());
        }

        // 未来可以从其他字段获取更详细的营养信息
        nutrition.setProtein(0.0);
        nutrition.setFat(0.0);
        nutrition.setCarbs(0.0);

        return nutrition;
    }

    /**
     * 提取烹饪方式
     */
    private String extractCookingMethod(Dish dish) {
        if (dish.getDescription() == null) {
            return "炒";
        }

        String desc = dish.getDescription();

        if (desc.contains("蒸")) return "蒸";
        if (desc.contains("煮") || desc.contains("汤")) return "煮";
        if (desc.contains("炸")) return "炸";
        if (desc.contains("烤")) return "烤";
        if (desc.contains("煎")) return "煎";

        return "炒"; // 默认
    }

    /**
     * 提取适用场景
     */
    private List<String> extractSuitableScenarios(Dish dish) {
        List<String> scenarios = new ArrayList<>();

        // 默认场景
        scenarios.add("工作日");

        // 基于价格判断
        if (dish.getPrice() != null) {
            if (dish.getPrice().compareTo(new BigDecimal("50")) > 0) {
                scenarios.add("聚餐");
                scenarios.add("宴请");
            } else {
                scenarios.add("快餐");
            }
        }

        return scenarios;
    }

    /**
     * 提取时段标签
     */
    private List<String> extractTimePeriodTags(Dish dish) {
        List<String> periods = new ArrayList<>();

        // 默认所有时段都适合
        periods.add("午餐");
        periods.add("晚餐");

        return periods;
    }

    /**
     * 提取季节标签
     */
    private List<String> extractSeasonTags(Dish dish) {
        List<String> seasons = new ArrayList<>();

        // 默认适合所有季节
        return seasons;
    }

    /**
     * 计算价格等级
     */
    private Integer calculatePriceLevel(BigDecimal price) {
        if (price == null) {
            return 3;
        }

        double priceValue = price.doubleValue();
        if (priceValue < 15) return 1;
        if (priceValue < 25) return 2;
        if (priceValue < 40) return 3;
        if (priceValue < 60) return 4;
        return 5;
    }

    /**
     * 保存相似度
     */
    private void saveSimilarity(String dishIdA, String dishIdB, double score, String similarityType) {
        // 删除旧的相似度记录
        dishSimilarityMapper.deleteByDishIdAndType(dishIdA, similarityType);

        // 插入新的相似度记录
        DishSimilarity similarity = new DishSimilarity();
        similarity.setDishIdA(dishIdA);
        similarity.setDishIdB(dishIdB);
        similarity.setSimilarityScore(BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP));
        similarity.setSimilarityType(similarityType);
        similarity.setUpdatedTime(LocalDateTime.now());

        dishSimilarityMapper.insert(similarity);
    }

    /**
     * 计算标签相似度（Jaccard系数）
     */
    private double calculateTagSimilarity(List<String> tagsA, List<String> tagsB) {
        if (tagsA == null || tagsB == null || tagsA.isEmpty() || tagsB.isEmpty()) {
            return 0.0;
        }

        Set<String> setA = new HashSet<>(tagsA);
        Set<String> setB = new HashSet<>(tagsB);

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 计算风味相似度（余弦相似度）
     */
    private double calculateFlavorSimilarity(Map<String, Double> flavorA, Map<String, Double> flavorB) {
        if (flavorA == null || flavorB == null || flavorA.isEmpty() || flavorB.isEmpty()) {
            return 0.5; // 默认中等相似度
        }

        // 余弦相似度
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(flavorA.keySet());
        allKeys.addAll(flavorB.keySet());

        for (String key : allKeys) {
            double valueA = flavorA.getOrDefault(key, 0.0);
            double valueB = flavorB.getOrDefault(key, 0.0);

            dotProduct += valueA * valueB;
            normA += valueA * valueA;
            normB += valueB * valueB;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator > 0 ? dotProduct / denominator : 0.0;
    }

    /**
     * 计算价格相似度
     */
    private double calculatePriceSimilarity(Integer levelA, Integer levelB) {
        if (levelA == null || levelB == null) {
            return 0.5;
        }

        int diff = Math.abs(levelA - levelB);
        return 1.0 - (diff / 4.0); // 价格等级差越小，相似度越高
    }

    /**
     * 计算营养相似度
     */
    private double calculateNutritionSimilarity(DishFeature.NutritionInfo nutritionA, DishFeature.NutritionInfo nutritionB) {
        if (nutritionA == null || nutritionB == null) {
            return 0.5;
        }

        // 简化实现：基于卡路里差异
        double calA = nutritionA.getCalories() != null ? nutritionA.getCalories() : 0;
        double calB = nutritionB.getCalories() != null ? nutritionB.getCalories() : 0;

        double maxCal = Math.max(calA, calB);
        double diff = Math.abs(calA - calB);

        return maxCal > 0 ? 1.0 - (diff / maxCal) : 1.0;
    }
}
