package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 销售趋势项DTO
 */
@Data
public class SalesTrendItemDTO {

    /**
     * 时间标签（如：周一、1日等）
     */
    private String label;

    /**
     * 销售额
     */
    private Double value;
}
