package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 推荐反馈DTO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class RecommendFeedbackDTO {

    /**
     * 推荐记录ID
     */
    @NotNull(message = "推荐记录ID不能为空")
    private String recommendHistoryId;

    /**
     * 是否点击
     */
    private Boolean isClicked;

    /**
     * 是否下单
     */
    private Boolean isOrdered;

    /**
     * 反馈评分（1-5）
     */
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer feedbackScore;
}
