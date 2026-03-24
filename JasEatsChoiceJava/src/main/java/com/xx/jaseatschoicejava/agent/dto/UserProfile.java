package com.xx.jaseatschoicejava.agent.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

/**
 * 用户详细资料DTO
 *
 * 用于Agent工具类返回用户详细信息
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class UserProfile {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 身高（cm）
     */
    private Double height;

    /**
     * 体重（kg）
     */
    private Double weight;

    /**
     * 性别
     */
    private String gender;

    /**
     * 饮食目标
     */
    private String dietGoal;

    /**
     * 偏好标签（JSON格式）
     */
    private JsonNode preferTags;

    /**
     * 过敏食材（JSON格式）
     */
    private JsonNode allergies;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 总消费金额
     */
    private Double totalSpending;

    /**
     * 用户是否存在
     */
    private Boolean exists;
}
