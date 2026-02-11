package com.xx.jaseatschoicejava.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容源VO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class ContentSourceVO {

    /**
     * 内容源ID
     */
    private String id;

    /**
     * 内容URL
     */
    private String contentUrl;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 内容类型名称
     */
    private String contentTypeName;

    /**
     * 平台
     */
    private String platform;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 内容标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 视频时长（秒）
     */
    private Integer videoDuration;

    /**
     * 视频时长格式化（如 "5:30"）
     */
    private String videoDurationFormatted;

    /**
     * 内容描述
     */
    private String description;

    /**
     * 是否已提取
     */
    private Boolean isExtracted;

    /**
     * 提取ID（用于查看详情）
     */
    private String extractionId;

    /**
     * 提取状态
     */
    private String extractionStatus;

    /**
     * 提取状态名称
     */
    private String extractionStatusName;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 提取的菜品名称
     */
    private String extractedDishName;

    /**
     * 提取的菜品图片
     */
    private String extractedDishImage;

    /**
     * 是否已发布为食谱
     */
    private Boolean isPublished;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 提取时间
     */
    private LocalDateTime extractionTime;
}
