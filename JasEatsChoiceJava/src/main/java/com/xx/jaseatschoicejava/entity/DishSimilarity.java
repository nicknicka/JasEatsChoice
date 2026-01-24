package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品相似度实体
 * 存储预计算的菜品相似度，用于协同过滤和"相似菜品"推荐
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("dish_similarity")
public class DishSimilarity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 菜品A ID
     */
    private String dishIdA;

    /**
     * 菜品B ID
     */
    private String dishIdB;

    /**
     * 相似度分数 (0-1)
     * 0表示完全不同，1表示完全相同
     */
    private BigDecimal similarityScore;

    /**
     * 相似度类型
     * - content: 基于内容（特征、标签等）
     * - collaborative: 协同过滤（基于用户行为）
     * - hybrid: 混合方法
     */
    private String similarityType;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 相似度类型枚举
     */
    public enum SimilarityType {
        CONTENT("content", "基于内容"),
        COLLABORATIVE("collaborative", "协同过滤"),
        HYBRID("hybrid", "混合方法");

        private final String code;
        private final String desc;

        SimilarityType(String code, String desc) {
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

    /**
     * 判断是否为高相似度
     */
    public boolean isHighSimilarity() {
        return similarityScore != null && similarityScore.compareTo(new BigDecimal("0.7")) >= 0;
    }

    /**
     * 判断是否为中等相似度
     */
    public boolean isMediumSimilarity() {
        return similarityScore != null &&
                similarityScore.compareTo(new BigDecimal("0.4")) >= 0 &&
                similarityScore.compareTo(new BigDecimal("0.7")) < 0;
    }
}
