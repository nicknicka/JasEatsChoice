package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 评价分布DTO
 */
@Data
public class RatingDistributionDTO {

    /**
     * 星级
     */
    private Integer stars;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 百分比
     */
    private Integer percent;
}
