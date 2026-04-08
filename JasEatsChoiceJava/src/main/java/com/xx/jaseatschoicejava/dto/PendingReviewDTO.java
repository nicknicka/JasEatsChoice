package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 待回复评价DTO
 */
@Data
public class PendingReviewDTO {

    /**
     * 评价ID
     */
    private String id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价时间
     */
    private String time;

    /**
     * 是否已回复
     */
    private Boolean replied;
}
