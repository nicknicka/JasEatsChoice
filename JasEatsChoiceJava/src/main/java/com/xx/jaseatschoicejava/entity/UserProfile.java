package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户画像实体
 * 存储用户的偏好标签、饮食禁忌、口味偏好等特征信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_profile")
public class UserProfile {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 偏好标签: [{"tag": "川菜", "score": 0.8}, ...]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PreferenceTag> preferenceTags;

    /**
     * 饮食禁忌: ["过敏原", "宗教禁忌"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> dietaryRestrictions;

    /**
     * 饮食目标: low_calorie(低卡), high_protein(高蛋白), balanced(均衡)
     */
    private String dietGoal;

    /**
     * 口味偏好: {"spicy": 0.7, "sweet": 0.3, "salty": 0.5}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Double> flavorPreference;

    /**
     * 价格偏好: {"min": 10, "max": 50, "optimal": 25}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private PricePreference pricePreference;

    /**
     * 营养目标: {"calories": 2000, "protein": 100}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private NutritionGoals nutritionGoals;

    /**
     * 用餐模式: {"breakfast": "07:00", "lunch": "12:00", "dinner": "18:30"}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> mealPattern;

    /**
     * 统计数据: {total_orders, avg_order_amount, fav_categories}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private UserStatistics statistics;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;

    /**
     * 偏好标签内部类
     */
    @Data
    public static class PreferenceTag {
        /**
         * 标签名称
         */
        private String tag;

        /**
         * 偏好分数 (0-1)
         */
        private Double score;
    }

    /**
     * 价格偏好内部类
     */
    @Data
    public static class PricePreference {
        /**
         * 最低价格
         */
        private Double min;

        /**
         * 最高价格
         */
        private Double max;

        /**
         * 最优价格
         */
        private Double optimal;
    }

    /**
     * 营养目标内部类
     */
    @Data
    public static class NutritionGoals {
        /**
         * 卡路里目标
         */
        private Double calories;

        /**
         * 蛋白质目标 (克)
         */
        private Double protein;

        /**
         * 脂肪目标 (克)
         */
        private Double fat;

        /**
         * 碳水化合物目标 (克)
         */
        private Double carbs;
    }

    /**
     * 用户统计数据内部类
     */
    @Data
    public static class UserStatistics {
        /**
         * 总订单数
         */
        private Integer totalOrders;

        /**
         * 平均订单金额
         */
        private Double avgOrderAmount;

        /**
         * 喜爱的类别列表
         */
        private List<String> favCategories;

        /**
         * 最常下单的时段
         */
        private String mostFrequentTimePeriod;

        /**
         * 平均每次下单菜品数
         */
        private Double avgItemsPerOrder;
    }
}
