package com.xx.jaseatschoicejava.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户自定义事件VO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class UserEventVO {

    /**
     * 事件ID
     */
    private String id;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件类型图标
     */
    private String eventTypeIcon;

    /**
     * 事件日期
     */
    private String eventDate;

    /**
     * 年份
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
     * 偏好菜品列表
     */
    private List<String> preferredDishes;

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
     * 距离事件天数
     */
    private Integer daysUntilEvent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
