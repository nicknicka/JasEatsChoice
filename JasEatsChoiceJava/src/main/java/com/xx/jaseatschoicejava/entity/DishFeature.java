package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 菜品特征实体
 * 存储菜品的详细特征信息，用于推荐匹配和相似度计算
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("dish_features")
public class DishFeature {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 菜品分类
     */
    private String category;

    /**
     * 标签数组: ["辣", "川菜", "下饭菜"]
     */
    private List<String> tags;

    /**
     * 风味特征: {"spicy": 0.8, "salty": 0.5}
     * 范围: 0-1之间，0表示无此风味，1表示此风味很强
     */
    private Map<String, Double> flavorProfile;

    /**
     * 营养信息: {calories, protein, fat, carbs}
     */
    private NutritionInfo nutritionInfo;

    /**
     * 主要食材: ["鸡肉", "土豆", "辣椒"]
     */
    private List<String> ingredients;

    /**
     * 烹饪方式: 炒/煮/蒸/烤/炸等
     */
    private String cookingMethod;

    /**
     * 适用场景: ["工作日", "聚餐", "宵夜"]
     */
    private List<String> suitableScenarios;

    /**
     * 时段标签: ["早餐", "午餐", "晚餐", "宵夜"]
     */
    private List<String> timePeriodTags;

    /**
     * 季节标签: ["春季", "夏季", "秋季", "冬季"]
     */
    private List<String> seasonTags;

    /**
     * 价格等级 1-5
     * 1: 0-15元
     * 2: 15-25元
     * 3: 25-40元
     * 4: 40-60元
     * 5: 60元以上
     */
    private Integer priceLevel;

    /**
     * 热度分数 (0-1)
     * 基于浏览、下单、收藏等行为综合计算
     */
    private BigDecimal popularityScore;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 营养信息内部类
     */
    @Data
    public static class NutritionInfo {
        /**
         * 卡路里
         */
        private Double calories;

        /**
         * 蛋白质
         */
        private Double protein;

        /**
         * 脂肪
         */
        private Double fat;

        /**
         * 碳水化合物
         */
        private Double carbs;

        /**
         * 膳食纤维
         */
        private Double fiber;

        /**
         * 钠
         */
        private Double sodium;
    }

    /**
     * 判断菜品是否适合当前时间
     */
    public boolean isSuitableForTime(String timePeriod) {
        return timePeriodTags != null && timePeriodTags.contains(timePeriod);
    }

    /**
     * 判断菜品是否适合当前季节
     */
    public boolean isSuitableForSeason(String season) {
        return seasonTags == null || seasonTags.isEmpty() || seasonTags.contains(season);
    }

    /**
     * 获取主要风味（分数最高的）
     */
    public String getPrimaryFlavor() {
        if (flavorProfile == null || flavorProfile.isEmpty()) {
            return "清淡";
        }

        return flavorProfile.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("清淡");
    }

    /**
     * 判断是否为辣味菜品
     */
    public boolean isSpicy() {
        return flavorProfile != null &&
                flavorProfile.containsKey("spicy") &&
                flavorProfile.get("spicy") > 0.5;
    }

    /**
     * 判断是否为低卡菜品（小于400卡路里）
     */
    public boolean isLowCalorie() {
        return nutritionInfo != null &&
                nutritionInfo.getCalories() != null &&
                nutritionInfo.getCalories() < 400;
    }

    /**
     * 判断是否为高蛋白菜品（蛋白质大于20克）
     */
    public boolean isHighProtein() {
        return nutritionInfo != null &&
                nutritionInfo.getProtein() != null &&
                nutritionInfo.getProtein() > 20;
    }
}
