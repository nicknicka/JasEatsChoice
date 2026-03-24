package com.xx.jaseatschoicejava.agent.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户健康目标DTO
 *
 * 用于Agent工具类返回用户健康目标信息
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class UserHealthGoal {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 目标类型（减肥、增肌、保持、增重）
     */
    private String goalType;

    /**
     * 当前体重
     */
    private Double currentWeight;

    /**
     * 目标体重
     */
    private Double targetWeight;

    /**
     * 每日热量目标
     */
    private Integer dailyCalorieTarget;

    /**
     * 目标期限（周）
     */
    private Integer deadlineWeeks;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 当前进度（已完成的百分比）
     */
    private Double progressPercentage;

    /**
     * 状态（进行中、已完成、已放弃）
     */
    private String status;

    /**
     * 是否存在
     */
    private Boolean exists;
}
