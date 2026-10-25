package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * 今日热点实体类
 */
@TableName("hot_topic")
@ApiModel(description = "今日热点实体")
public class HotTopic {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @ApiModelProperty(value = "热点内容")
    private String content;

    @ApiModelProperty(value = "优先级，数值越大优先级越高")
    private Integer priority;

    @ApiModelProperty(value = "来源类型: MANUAL-手动设置, TUTORIAL-来自教程, AI-AI生成, API-第三方API")
    private String sourceType;

    @ApiModelProperty(value = "来源ID（教程ID等）")
    private String sourceId;

    @ApiModelProperty(value = "生效开始时间")
    private LocalDateTime startDate;

    @ApiModelProperty(value = "生效结束时间")
    private LocalDateTime endDate;

    @ApiModelProperty(value = "状态: ACTIVE-生效, INACTIVE-未生效, EXPIRED-已过期")
    private String status;

    @ApiModelProperty(value = "点击次数")
    private Integer clickCount;

    @ApiModelProperty(value = "分享次数")
    private Integer shareCount;

    @ApiModelProperty(value = "是否需要审核")
    private Boolean requireReview;

    @ApiModelProperty(value = "审核状态: PENDING-待审核, APPROVED-通过, REJECTED-拒绝")
    private String reviewStatus;

    @ApiModelProperty(value = "审核人ID")
    private Long reviewerId;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime reviewTime;

    @ApiModelProperty(value = "审核意见")
    private String reviewComment;

    @ApiModelProperty(value = "备注信息")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人ID")
    private Long createBy;

    @ApiModelProperty(value = "更新人ID")
    private Long updateBy;

    // ========== Getter and Setter Methods ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Boolean getRequireReview() {
        return requireReview;
    }

    public void setRequireReview(Boolean requireReview) {
        this.requireReview = requireReview;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    // ========== 枚举定义 ==========

    /**
     * 来源类型枚举
     */
    public enum SourceType {
        MANUAL("MANUAL", "手动设置"),
        TUTORIAL("TUTORIAL", "来自教程"),
        AI("AI", "AI生成"),
        API("API", "第三方API");

        private final String code;
        private final String desc;

        SourceType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static SourceType fromCode(String code) {
            for (SourceType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown source type: " + code);
        }
    }

    /**
     * 状态枚举
     */
    public enum Status {
        ACTIVE("ACTIVE", "生效"),
        INACTIVE("INACTIVE", "未生效"),
        EXPIRED("EXPIRED", "已过期");

        private final String code;
        private final String desc;

        Status(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Status fromCode(String code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status: " + code);
        }
    }

    /**
     * 审核状态枚举
     */
    public enum ReviewStatus {
        PENDING("PENDING", "待审核"),
        APPROVED("APPROVED", "通过"),
        REJECTED("REJECTED", "拒绝");

        private final String code;
        private final String desc;

        ReviewStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static ReviewStatus fromCode(String code) {
            for (ReviewStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown review status: " + code);
        }
    }

    // ========== 便捷方法 ==========

    /**
     * 判断是否当前有效
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return Status.ACTIVE.getCode().equals(status)
                && (startDate == null || !startDate.isAfter(now))
                && (endDate == null || !endDate.isBefore(now));
    }

    /**
     * 判断是否需要审核
     */
    public boolean needsReview() {
        return requireReview != null && requireReview
                && !ReviewStatus.APPROVED.getCode().equals(reviewStatus);
    }
}
