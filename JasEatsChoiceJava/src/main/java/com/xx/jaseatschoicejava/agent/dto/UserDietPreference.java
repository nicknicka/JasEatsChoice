package com.xx.jaseatschoicejava.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

/**
 * 用户饮食偏好DTO
 *
 * 用于Agent工具类返回用户饮食偏好信息
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class UserDietPreference {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 辣度偏好（1-5级）
     */
    private Integer spicyLevel;

    /**
     * 甜度偏好（1-5级）
     */
    private Integer sweetLevel;

    /**
     * 菜系偏好（如：川菜、粤菜、湘菜等）
     */
    private String cuisinePreference;

    /**
     * 饮食类型（素食/荤食/混合）
     */
    private String dietType;

    /**
     * 价格区间（低/中/高）
     */
    private String priceRange;

    /**
     * 营养需求（如：低卡、低脂、高蛋白等）
     */
    private String nutritionNeeds;

    /**
     * 过敏食材列表
     */
    private JsonNode allergies;

    /**
     * 偏好标签（原始JSON数据）
     */
    private JsonNode preferTags;

    /**
     * 用户是否存在
     */
    private Boolean exists;
}
