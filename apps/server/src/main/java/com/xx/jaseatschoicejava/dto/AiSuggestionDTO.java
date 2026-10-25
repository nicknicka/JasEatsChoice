package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * AI建议DTO
 */
@Data
public class AiSuggestionDTO {

    /**
     * 建议类型：warning/success/opportunity
     */
    private String type;

    /**
     * 建议内容
     */
    private String content;
}
