package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 菜品步骤更新DTO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "菜品步骤更新请求")
public class DishStepUpdateDTO {

    @ApiModelProperty(value = "订单菜品ID列表（批量更新时使用）", required = true)
    private List<String> orderDishIds;

    @ApiModelProperty(value = "订单菜品ID（单个更新时使用）")
    private String orderDishId;

    @ApiModelProperty(value = "新步骤状态", required = true)
    @NotNull(message = "步骤状态不能为空")
    private Integer newStepStatus;

    @ApiModelProperty(value = "操作类型：FORWARD-前进, BACKWARD-回退, SKIP-跳过")
    private String operationType = "FORWARD";

    @ApiModelProperty(value = "回退原因（回退操作必填）")
    private String rollbackReason;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "预计完成时间（分钟）")
    private Integer estimatedMinutes;

    @ApiModelProperty(value = "步骤排序（批量更新时使用，拖拽排序）")
    private List<StepSortItem> stepSortItems;

    @Data
    public static class StepSortItem {
        @ApiModelProperty(value = "订单菜品ID", required = true)
        @NotBlank(message = "订单菜品ID不能为空")
        private String orderDishId;

        @ApiModelProperty(value = "排序值", required = true)
        @NotNull(message = "排序值不能为空")
        private Integer sort;
    }
}
