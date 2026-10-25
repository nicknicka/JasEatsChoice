package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Data
@TableName("user_coupon")
public class UserCoupon {
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "优惠券ID")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "最低消费金额")
    private BigDecimal minAmount;

    @ApiModelProperty(value = "优惠券状态")
    private String status; // available-可用, used-已使用, expired-已过期

    @ApiModelProperty(value = "关联订单ID")
    private String orderId;

    @ApiModelProperty(value = "过期时间")
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "使用时间")
    private LocalDateTime useTime;
}
