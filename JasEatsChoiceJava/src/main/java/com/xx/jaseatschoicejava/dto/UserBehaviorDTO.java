package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.util.Map;

/**
 * 用户行为记录DTO
 */
@Data
public class UserBehaviorDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 行为类型: view/click/order/favorite/reject/share
     */
    private String behaviorType;

    /**
     * 物品类型: dish/merchant/recipe
     */
    private String itemType;

    /**
     * 物品ID
     */
    private String itemId;

    /**
     * 上下文信息
     */
    private Map<String, Object> context;

    /**
     * 行为持续时长(秒)
     */
    private Integer duration;
}
