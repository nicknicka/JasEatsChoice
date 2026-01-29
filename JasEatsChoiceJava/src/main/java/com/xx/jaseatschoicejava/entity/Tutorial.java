package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 教程实体类
 * 支持三种数据来源：管理员(ADMIN)、商家(MERCHANT)、AI生成(AI_GENERATED)
 */
@Data
@TableName("tutorial")
public class Tutorial {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    // ========== 基础信息 ==========
    private String title; // 教程标题
    private String type; // 教程类型: video-视频, article-图文
    private String duration; // 时长
    private String views; // 浏览数（旧字段，保留兼容）

    // ========== 来源信息 ==========
    private String sourceType; // 来源类型: ADMIN-管理员, MERCHANT-商家, AI_GENERATED-AI生成
    private Long sourceId; // 来源ID: 管理员ID/商家ID/AI版本
    private String authorType; // 作者类型: ADMIN, MERCHANT, AI
    private Long authorId; // 作者ID
    private String author; // 作者显示名称

    // ========== 状态管理 ==========
    private String status; // 状态: DRAFT-草稿, PENDING-待审核, PUBLISHED-已发布, REJECTED-已拒绝
    private String reviewStatus; // 审核状态: NOT_SUBMITTED-未提交, PENDING-待审核, APPROVED-通过, REJECTED-拒绝
    private Long reviewerId; // 审核人ID
    private Date reviewTime; // 审核时间
    private String reviewComment; // 审核意见
    private boolean featured; // 是否精选
    private boolean isOfficial; // 是否官方认证（仅管理员）

    // ========== 关联信息 ==========
    private Long linkedMerchantId; // 关联商家ID（商家教程可用）
    private Long linkedDishId; // 关联菜品ID（商家教程可用）
    private String aiModelVersion; // AI模型版本（AI教程）

    // ========== 内容 ==========
    private String content; // 教程内容
    private String coverImage; // 封面图
    private String videoUrl; // 视频URL
    private String tags; // 标签 JSON格式: ["健康", "低卡", "素食"]

    // ========== 扩展信息 ==========
    private String difficulty; // 难度: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级
    private Integer calories; // 卡路里
    private String prepTime; // 准备时间
    private Integer servings; // 份量

    // ========== 统计数据 ==========
    private BigDecimal rating; // 评分（0-5）
    private Integer ratingCount; // 评分人数
    private Integer favoriteCount; // 收藏数
    private Integer viewCount; // 浏览数
    private Integer shareCount; // 分享数

    // ========== 时间戳 ==========
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间

    // ========== 枚举定义 ==========

    /**
     * 作者类型枚举
     */
    public enum AuthorType {
        ADMIN("ADMIN", "管理员"),
        MERCHANT("MERCHANT", "商家"),
        USER("USER", "用户"),
        AI("AI", "AI");

        private final String code;
        private final String desc;

        AuthorType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() { return code; }
        public String getDesc() { return desc; }

        public static AuthorType fromCode(String code) {
            for (AuthorType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown author type: " + code);
        }
    }

    /**
     * 来源类型枚举
     */
    public enum SourceType {
        ADMIN("ADMIN", "管理员"),
        MERCHANT("MERCHANT", "商家"),
        USER("USER", "用户"),
        AI_GENERATED("AI_GENERATED", "AI生成");

        private final String code;
        private final String desc;

        SourceType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() { return code; }
        public String getDesc() { return desc; }

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
        DRAFT("DRAFT", "草稿"),
        PENDING("PENDING", "待审核"),
        PUBLISHED("PUBLISHED", "已发布"),
        REJECTED("REJECTED", "已拒绝");

        private final String code;
        private final String desc;

        Status(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() { return code; }
        public String getDesc() { return desc; }

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
        NOT_SUBMITTED("NOT_SUBMITTED", "未提交"),
        PENDING("PENDING", "待审核"),
        APPROVED("APPROVED", "已通过"),
        REJECTED("REJECTED", "已拒绝");

        private final String code;
        private final String desc;

        ReviewStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() { return code; }
        public String getDesc() { return desc; }

        public static ReviewStatus fromCode(String code) {
            for (ReviewStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown review status: " + code);
        }
    }

    /**
     * 难度枚举
     */
    public enum Difficulty {
        BEGINNER("BEGINNER", "初级"),
        INTERMEDIATE("INTERMEDIATE", "中级"),
        ADVANCED("ADVANCED", "高级");

        private final String code;
        private final String desc;

        Difficulty(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() { return code; }
        public String getDesc() { return desc; }

        public static Difficulty fromCode(String code) {
            for (Difficulty difficulty : values()) {
                if (difficulty.code.equals(code)) {
                    return difficulty;
                }
            }
            throw new IllegalArgumentException("Unknown difficulty: " + code);
        }
    }

    // ========== Getter and Setter Methods ==========

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getAuthorType() {
        return authorType;
    }

    public void setAuthorType(String authorType) {
        this.authorType = authorType;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setOfficial(boolean isOfficial) {
        this.isOfficial = isOfficial;
    }

    public Long getLinkedMerchantId() {
        return linkedMerchantId;
    }

    public void setLinkedMerchantId(Long linkedMerchantId) {
        this.linkedMerchantId = linkedMerchantId;
    }

    public Long getLinkedDishId() {
        return linkedDishId;
    }

    public void setLinkedDishId(Long linkedDishId) {
        this.linkedDishId = linkedDishId;
    }

    public String getAiModelVersion() {
        return aiModelVersion;
    }

    public void setAiModelVersion(String aiModelVersion) {
        this.aiModelVersion = aiModelVersion;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    // ========== 便捷方法 ==========

    /**
     * 判断是否可以编辑
     */
    public boolean isEditable() {
        return !Status.PUBLISHED.getCode().equals(status) ||
               ReviewStatus.PENDING.getCode().equals(reviewStatus);
    }

    /**
     * 判断是否已发布
     */
    public boolean isPublished() {
        return Status.PUBLISHED.getCode().equals(status);
    }

    /**
     * 判断是否为官方认证
     */
    public boolean isAdminSource() {
        return SourceType.ADMIN.getCode().equals(sourceType);
    }

    /**
     * 判断是否为商家来源
     */
    public boolean isMerchantSource() {
        return SourceType.MERCHANT.getCode().equals(sourceType);
    }

    /**
     * 判断是否为AI生成
     */
    public boolean isAIGenerated() {
        return SourceType.AI_GENERATED.getCode().equals(sourceType);
    }

    /**
     * 判断是否为用户来源
     */
    public boolean isUserSource() {
        return SourceType.USER.getCode().equals(sourceType);
    }

    /**
     * 判断是否需要审核
     */
    public boolean needsReview() {
        return !ReviewStatus.APPROVED.getCode().equals(reviewStatus);
    }

    /**
     * 判断是否可以设置为精选
     */
    public boolean canBeFeatured() {
        return isPublished() && ReviewStatus.APPROVED.getCode().equals(reviewStatus);
    }
}
