package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 内容源创建DTO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class ContentSourceCreateDTO {

    /**
     * 内容URL
     */
    @NotBlank(message = "内容URL不能为空")
    private String contentUrl;

    /**
     * 内容类型（可选，系统会自动识别）
     */
    private String contentType;

    /**
     * 平台（可选，系统会自动识别）
     */
    private String platform;
}
