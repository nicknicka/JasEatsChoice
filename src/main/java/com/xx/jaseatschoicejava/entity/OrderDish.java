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

/**
 * 订单菜品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_dish")
@ApiModel(description = "订单菜品实体")
public class OrderDish {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "订单菜品ID")
    private Long id; // 订单菜品ID

    @TableField("order_id")
    @ApiModelProperty(value = "订单ID")
    private Long orderId; // 订单ID

    @TableField("dish_id")
    @ApiModelProperty(value = "菜品ID")
    private Long dishId; // 菜品ID

    @TableField("quantity")
    @ApiModelProperty(value = "菜品数量")
    private Integer quantity; // 数量

    @TableField("price")
    @ApiModelProperty(value = "菜品单价")
    private BigDecimal price; // 单价

    @TableField("customization")
    @ApiModelProperty(value = "菜品定制要求")
    private String customization; // 定制要求
}