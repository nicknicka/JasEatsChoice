package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容源实体类
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
@TableName("t_content_source")
public class ContentSource {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 内容URL
     */
    private String contentUrl;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 平台
     */
    private String platform;

    /**
     * 内容标题
     */
    private String title;

    /**
     * 作者/UP主
     */
    private String author;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 视频时长（秒）
     */
    private Integer videoDuration;

    /**
     * 内容描述
     */
    private String description;

    /**
     * 是否已提取
     */
    private Boolean isExtracted;

    /**
     * 提取状态
     */
    private String extractionStatus;

    /**
     * 提取时间
     */
    private LocalDateTime extractionTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
