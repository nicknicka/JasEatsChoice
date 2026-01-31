package com.xx.jaseatschoicejava.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品步骤详情VO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "菜品步骤详情")
public class DishStepDetailVO {

    @ApiModelProperty(value = "订单菜品ID")
    private String orderDishId;

    @ApiModelProperty(value = "订单ID")
    private String orderId;

    @ApiModelProperty(value = "菜品ID")
    private String dishId;

    @ApiModelProperty(value = "菜品名称")
    private String dishName;

    @ApiModelProperty(value = "菜品图片")
    private String dishImage;

    @ApiModelProperty(value = "菜品数量")
    private Integer quantity;

    @ApiModelProperty(value = "当前步骤状态")
    private Integer stepStatus;

    @ApiModelProperty(value = "当前步骤名称")
    private String stepStatusName;

    @ApiModelProperty(value = "步骤开始时间")
    private LocalDateTime stepStartTime;

    @ApiModelProperty(value = "预计完成时间")
    private LocalDateTime estimatedCompletionTime;

    @ApiModelProperty(value = "烹饪耗时（分钟）")
    private Integer cookingMinutes;

    @ApiModelProperty(value = "步骤排序")
    private Integer stepSort;

    @ApiModelProperty(value = "是否为快餐")
    private Boolean isFastFood;

    @ApiModelProperty(value = "上菜状态")
    private Integer servingStatus;

    @ApiModelProperty(value = "已用时间（分钟）")
    private Long elapsedMinutes;

    @ApiModelProperty(value = "剩余时间（分钟）")
    private Long remainingMinutes;

    @ApiModelProperty(value = "步骤进度百分比")
    private Integer progressPercent;

    @ApiModelProperty(value = "步骤历史记录")
    private List<StepHistoryItem> stepHistory;

    @Data
    public static class StepHistoryItem {
        @ApiModelProperty(value = "历史记录ID")
        private String id;

        @ApiModelProperty(value = "原步骤状态")
        private Integer oldStepStatus;

        @ApiModelProperty(value = "原步骤名称")
        private String oldStepStatusName;

        @ApiModelProperty(value = "新步骤状态")
        private Integer newStepStatus;

        @ApiModelProperty(value = "新步骤名称")
        private String newStepStatusName;

        @ApiModelProperty(value = "操作类型")
        private String operationType;

        @ApiModelProperty(value = "操作人ID")
        private String operatorId;

        @ApiModelProperty(value = "操作人姓名")
        private String operatorName;

        @ApiModelProperty(value = "回退原因")
        private String rollbackReason;

        @ApiModelProperty(value = "创建时间")
        private LocalDateTime createTime;

        @ApiModelProperty(value = "预计耗时（分钟）")
        private Integer estimatedMinutes;
    }
}
