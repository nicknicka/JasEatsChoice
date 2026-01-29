package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 今日热点响应DTO
 */
@ApiModel(description = "今日热点响应")
public class HotTopicResponse {

    @ApiModelProperty(value = "热点内容")
    private String content;

    @ApiModelProperty(value = "来源类型: MANUAL-手动设置, TUTORIAL-来自教程, AI-AI生成, API-第三方API")
    private String sourceType;

    @ApiModelProperty(value = "来源ID（如教程ID）")
    private String sourceId;

    @ApiModelProperty(value = "跳转URL")
    private String redirectUrl;

    @ApiModelProperty(value = "是否可点击")
    private Boolean clickable;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    public HotTopicResponse() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Boolean getClickable() {
        return clickable;
    }

    public void setClickable(Boolean clickable) {
        this.clickable = clickable;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 创建教程来源的热点响应
     */
    public static HotTopicResponse fromTutorial(String content, String tutorialId) {
        HotTopicResponse response = new HotTopicResponse();
        response.setContent(content);
        response.setSourceType("TUTORIAL");
        response.setSourceId(tutorialId);
        response.setRedirectUrl("/user/home/tutorials/" + tutorialId);
        response.setClickable(true);
        return response;
    }

    /**
     * 创建手动配置的热点响应
     */
    public static HotTopicResponse fromManual(String content, String redirectUrl) {
        HotTopicResponse response = new HotTopicResponse();
        response.setContent(content);
        response.setSourceType("MANUAL");
        response.setRedirectUrl(redirectUrl);
        response.setClickable(redirectUrl != null && !redirectUrl.isEmpty());
        return response;
    }

    /**
     * 创建AI生成的热点响应
     */
    public static HotTopicResponse fromAI(String content) {
        HotTopicResponse response = new HotTopicResponse();
        response.setContent(content);
        response.setSourceType("AI");
        response.setClickable(false);
        return response;
    }
}
