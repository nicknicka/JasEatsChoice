package com.xx.jaseatschoicejava.agent.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项DTO
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class OrderItemDTO {

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 菜品名称
     */
    private String dishName;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 小计
     */
    private BigDecimal subtotal;
}
