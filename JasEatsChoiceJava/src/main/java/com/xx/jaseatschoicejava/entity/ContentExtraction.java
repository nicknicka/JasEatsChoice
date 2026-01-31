package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容提取实体类
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
@TableName("t_content_extraction")
public class ContentExtraction {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 内容源ID
     */
    private String sourceId;

    /**
     * 菜品名称
     */
    private String dishName;

    /**
     * 菜品图片
     */
    private String dishImage;

    /**
     * 菜品描述
     */
    private String description;

    /**
     * 食材列表（JSON格式）
     */
    private String ingredients;

    /**
     * 制作步骤（JSON格式）
     */
    private String steps;

    /**
     * 制作时长（分钟）
     */
    private Integer cookingTime;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 卡路里
     */
    private Integer calories;

    /**
     * 是否已发布为食谱
     */
    private Boolean isPublished;

    /**
     * 关联的食谱ID
     */
    private String recipeId;

    /**
     * 人工评分（1-5）
     */
    private Integer manualScore;

    /**
     * 是否人工验证
     */
    private Boolean isVerified;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
