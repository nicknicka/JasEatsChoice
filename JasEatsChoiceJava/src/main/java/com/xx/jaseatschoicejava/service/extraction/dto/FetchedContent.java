package com.xx.jaseatschoicejava.service.extraction.dto;

import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内容抓取结果 DTO
 * 统一封装从各平台抓取到的原始内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FetchedContent {

    /** 内容标题 */
    private String title;

    /** 作者/UP主 */
    private String author;

    /** 封面图片URL */
    private String coverImage;

    /** 提取的正文文字 */
    private String textContent;

    /** 提取的图片URL列表 */
    private List<String> imageUrls;

    /** 图片Base64列表（用于传给视觉模型） */
    private List<String> imageBase64List;

    /** 视频直链URL（用于传给GLM视频理解） */
    private String videoUrl;

    /** 视频Base64（备选，大视频慎用） */
    private String videoBase64;

    /** 实际内容类型 */
    private ContentType contentType;

    /** 来源平台 */
    private ContentPlatform platform;

    /** 是否抓取成功 */
    private boolean fetchSuccess;

    /** 错误信息 */
    private String errorMessage;

    /**
     * 判断是否有图片内容
     */
    public boolean hasImages() {
        return imageBase64List != null && !imageBase64List.isEmpty();
    }

    /**
     * 判断是否有视频内容
     */
    public boolean hasVideo() {
        return (videoUrl != null && !videoUrl.isEmpty()) ||
               (videoBase64 != null && !videoBase64.isEmpty());
    }

    /**
     * 判断是否有文字内容
     */
    public boolean hasText() {
        return textContent != null && !textContent.trim().isEmpty();
    }
}
