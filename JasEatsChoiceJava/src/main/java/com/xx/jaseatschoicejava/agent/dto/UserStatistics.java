package com.xx.jaseatschoicejava.agent.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 用户统计信息DTO
 *
 * 用于Agent工具类返回用户统计数据
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class UserStatistics {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 总消费金额
     */
    private Double totalSpending;

    /**
     * 平均订单金额
     */
    private Double averageOrderAmount;

    /**
     * 最常订购的菜品
     */
    private String favoriteDish;

    /**
     * 最近订购时间
     */
    private String lastOrderTime;

    /**
     * 会员等级
     */
    private String memberLevel;
}
