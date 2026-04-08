package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 热销菜品DTO
 */
@Data
public class TopDishDTO {

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 变化趋势百分比
     */
    private Integer trend;
}
