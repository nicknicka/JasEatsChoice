package com.xx.jaseatschoicejava.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 经营洞察指标DTO
 */
@Data
public class InsightMetricsDTO {

    /**
     * 营业额
     */
    private BigDecimal revenue;

    /**
     * 营业额变化百分比
     */
    private Double revenueChange;

    /**
     * 订单数
     */
    private Long orders;

    /**
     * 订单数变化百分比
     */
    private Double ordersChange;

    /**
     * 客单价
     */
    private BigDecimal averagePrice;

    /**
     * 客单价变化百分比
     */
    private Double averageChange;

    /**
     * 平均评分
     */
    private Double rating;

    /**
     * 评分变化
     */
    private Double ratingChange;
}
