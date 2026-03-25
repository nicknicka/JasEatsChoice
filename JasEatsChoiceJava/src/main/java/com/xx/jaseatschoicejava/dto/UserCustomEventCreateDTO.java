package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户自定义事件创建DTO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class UserCustomEventCreateDTO {

    /**
     * 事件名称
     */
    @NotBlank(message = "事件名称不能为空")
    private String eventName;

    /**
     * 事件类型
     */
    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    /**
     * 事件日期（MM-dd格式）
     */
    @NotBlank(message = "事件日期不能为空")
    private String eventDate;

    /**
     * 年份（NULL表示每年重复）
     */
    private Integer year;

    /**
     * 提前提醒天数
     */
    private Integer reminderDays;

    /**
     * 事件描述
     */
    private String description;

    /**
     * 偏好菜品ID列表
     */
    private List<String> preferredDishIds;

    /**
     * 预计用餐人数
     */
    private Integer guestCount;

    /**
     * 人均预算
     */
    private java.math.BigDecimal budgetPerPerson;
}
