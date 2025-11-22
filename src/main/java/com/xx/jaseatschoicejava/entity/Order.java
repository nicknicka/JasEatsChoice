package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 订单实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order")
@ApiModel(description = "订单实体")
public class Order {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "订单ID")
    private Long id; // 订单ID

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private Long userId; // 用户ID

    @TableField("merchant_id")
    @ApiModelProperty(value = "商家ID")
    private Long merchantId; // 商家ID

    @TableField("total_amount")
    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount; // 订单总金额

    @TableField("status")
    @ApiModelProperty(value = "订单状态：0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成、6-已取消")
    private Integer status; // 订单状态：0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成、6-已取消

    @TableField("address")
    @ApiModelProperty(value = "配送地址")
    private String address; // 配送地址

    @TableField("remark")
    @ApiModelProperty(value = "订单备注")
    private String remark; // 订单备注

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}