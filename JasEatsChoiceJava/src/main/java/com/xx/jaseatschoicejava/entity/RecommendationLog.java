package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 推荐记录实体
 * 记录每次推荐的详情，用于效果分析和离线评估
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("recommendation_log")
public class RecommendationLog {

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
     * 推荐批次ID（同一次推荐的所有记录共享此ID）
     */
    private String recommendationId;

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 推荐排序位置
     */
    private Integer rank;

    /**
     * 推荐得分
     */
    private BigDecimal score;

    /**
     * 使用的算法: collaborative_filtering/content_based/hot/user_profile
     */
    private String algorithm;

    /**
     * 推荐理由: {primary: "主要理由", factors: [{type, name, score}]}
     */
    private RecommendationReason reason;

    /**
     * 是否被点击
     */
    private Boolean isClicked;

    /**
     * 是否被下单
     */
    private Boolean isOrdered;

    /**
     * 反馈时间
     */
    private LocalDateTime feedbackTime;

    /**
     * 推荐时间
     */
    private LocalDateTime createdTime;

    /**
     * 推荐理由内部类
     */
    @Data
    public static class RecommendationReason {
        /**
         * 主要推荐理由
         */
        private String primary;

        /**
         * 详细因素列表
         */
        private List<ReasonFactor> factors;
    }

    /**
     * 理由因素内部类
     */
    @Data
    public static class ReasonFactor {
        /**
         * 因素类型: user_preference/context/popularity/freshness/diversity
         */
        private String type;

        /**
         * 因素名称
         */
        private String name;

        /**
         * 因素得分 (0-1)
         */
        private Double score;
    }

    /**
     * 算法类型枚举
     */
    public enum Algorithm {
        COLLABORATIVE_FILTERING("collaborative_filtering", "协同过滤"),
        CONTENT_BASED("content_based", "基于内容"),
        HOT("hot", "热门推荐"),
        USER_PROFILE("user_profile", "用户画像"),
        HYBRID("hybrid", "混合推荐");

        private final String code;
        private final String desc;

        Algorithm(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
