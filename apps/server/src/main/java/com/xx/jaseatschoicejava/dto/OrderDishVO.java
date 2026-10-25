package com.xx.jaseatschoicejava.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xx.jaseatschoicejava.entity.Dish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单菜品VO类（包含菜品详细信息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_dish")
@ApiModel(description = "订单菜品VO（包含菜品详细信息）")
public class OrderDishVO {

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    @ApiModelProperty(value = "订单菜品ID")
    private String id; // 订单菜品ID

    @TableField("order_id")
    @ApiModelProperty(value = "订单ID")
    private String orderId; // 订单ID

    @TableField("dish_id")
    @ApiModelProperty(value = "菜品ID")
    private String dishId; // 菜品ID

    @ApiModelProperty(value = "菜品名称")
    private String dishName; // 菜品名称（从菜品表关联查询）

    @TableField("quantity")
    @ApiModelProperty(value = "菜品数量")
    private Integer quantity; // 数量

    @TableField("price")
    @ApiModelProperty(value = "菜品单价")
    private BigDecimal price; // 单价

    @TableField("customization")
    @ApiModelProperty(value = "菜品定制要求")
    private String customization; // 定制要求

    @ApiModelProperty(value = "菜品分类")
    private String category; // 菜品分类

    @ApiModelProperty(value = "卡路里含量")
    private Integer calorie; // 卡路里含量

    @ApiModelProperty(value = "菜品详细信息")
    private Dish dish; // 菜品完整信息对象
}
