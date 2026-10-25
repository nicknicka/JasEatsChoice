package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
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

    @TableId(type = IdType.INPUT)
    @ApiModelProperty(value = "订单ID")
    private String id; // 订单ID

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private String userId; // 用户ID

    @TableField("merchant_id")
    @ApiModelProperty(value = "商家ID")
    private String merchantId; // 商家ID

    @TableField(exist = false)  // 不映射到数据库字段
    @ApiModelProperty(value = "商家名称")
    private String merchantName; // 商家名称（用于前端显示）

    @TableField("total_amount")
    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount; // 订单总金额

    @TableField("status")
    @ApiModelProperty(value = "订单状态：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消")
    private Integer status; // 订单状态：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消

    @TableField(exist = false)  // 不映射到数据库字段
    @ApiModelProperty(value = "订单状态文本")
    private String statusText; // 订单状态文本（用于前端显示）

    @TableField("payment_id")
    @ApiModelProperty(value = "支付记录ID")
    private String paymentId; // 支付记录ID

    @TableField("paid_amount")
    @ApiModelProperty(value = "已支付金额")
    private BigDecimal paidAmount; // 已支付金额

    @TableField("payment_time")
    @ApiModelProperty(value = "支付时间")
    private LocalDateTime paymentTime; // 支付时间

    @TableField("address_id")
    @ApiModelProperty(value = "配送地址ID")
    private String addressId; // 配送地址ID

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