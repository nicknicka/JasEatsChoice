package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户自定义事件实体类
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
@TableName("t_user_custom_event")
public class UserCustomEvent {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件日期（MM-dd格式）
     */
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
     * 偏好菜品列表（JSON数组）
     */
    private String preferredDishes;

    /**
     * 预计用餐人数
     */
    private Integer guestCount;

    /**
     * 人均预算
     */
    private java.math.BigDecimal budgetPerPerson;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
