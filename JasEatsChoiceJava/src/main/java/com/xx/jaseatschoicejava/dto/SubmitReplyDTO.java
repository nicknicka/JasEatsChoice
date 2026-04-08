package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 评价回复提交DTO
 */
@Data
public class SubmitReplyDTO {

    /**
     * 评价ID
     */
    private String reviewId;

    /**
     * 商家ID
     */
    private String merchantId;

    /**
     * 回复内容
     */
    private String content;
}
