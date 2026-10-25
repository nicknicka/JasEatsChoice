package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单菜品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_dish")
@ApiModel(description = "订单菜品实体")
public class OrderDish {

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    @ApiModelProperty(value = "订单菜品ID")
    private String id; // 订单菜品ID

    @TableField("order_id")
    @ApiModelProperty(value = "订单ID")
    private String orderId; // 订单ID

    @TableField("dish_id")
    @ApiModelProperty(value = "菜品ID")
    private String dishId; // 菜品ID

    @TableField("quantity")
    @ApiModelProperty(value = "菜品数量")
    private Integer quantity; // 数量

    @TableField("price")
    @ApiModelProperty(value = "菜品单价")
    private BigDecimal price; // 单价

    @TableField("customization")
    @ApiModelProperty(value = "菜品定制要求")
    private String customization; // 定制要求

    @TableField("step_status")
    @ApiModelProperty(value = "当前步骤状态")
    private Integer stepStatus; // 步骤状态：0-待备菜 1-备菜中 2-预处理中 3-烹饪中 4-摆盘中 5-待上菜 6-已上菜 10-快餐制作中 11-快餐打包中 12-快餐待出餐 13-快餐已出餐

    @TableField("step_start_time")
    @ApiModelProperty(value = "当前步骤开始时间")
    private LocalDateTime stepStartTime; // 当前步骤开始时间

    @TableField("estimated_completion_time")
    @ApiModelProperty(value = "预计完成时间")
    private LocalDateTime estimatedCompletionTime; // 预计完成时间

    @TableField("cooking_minutes")
    @ApiModelProperty(value = "烹饪耗时（分钟）")
    private Integer cookingMinutes; // 烹饪预计耗时

    @TableField("step_sort")
    @ApiModelProperty(value = "步骤排序（优先级）")
    private Integer stepSort; // 步骤排序，数值越小越优先处理

    @TableField("is_fast_food")
    @ApiModelProperty(value = "是否为快餐")
    private Boolean isFastFood; // 是否为快餐：true-使用快餐流程，false-使用正餐流程

    @TableField("serving_status")
    @ApiModelProperty(value = "上菜状态")
    private Integer servingStatus; // 上菜状态：0-未上菜 1-已上菜 2-已撤餐
}