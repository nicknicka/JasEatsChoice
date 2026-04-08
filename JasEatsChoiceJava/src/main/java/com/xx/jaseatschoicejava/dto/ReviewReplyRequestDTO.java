package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 评价回复生成请求DTO
 */
@Data
public class ReviewReplyRequestDTO {

    /**
     * 评价ID
     */
    private String reviewId;

    /**
     * 评价内容
     */
    private String reviewContent;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 回复风格数量（默认3种）
     */
    private Integer styleCount = 3;
}
