package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_payment_record")
@ApiModel(description = "支付记录实体")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "支付记录ID")
    private Long id; // 支付记录ID

    @ApiModelProperty(value = "支付流水号")
    private String paymentNo; // 支付流水号

    @ApiModelProperty(value = "订单ID")
    private String orderId; // 订单ID

    @ApiModelProperty(value = "支付用户ID")
    private Long userId; // 支付用户ID

    @ApiModelProperty(value = "商家ID")
    private Long merchantId; // 商家ID

    @ApiModelProperty(value = "支付金额")
    private BigDecimal amount; // 支付金额

    @ApiModelProperty(value = "支付方式：wallet-钱包, wechat-微信, alipay-支付宝")
    private String paymentMethod; // 支付方式

    @ApiModelProperty(value = "支付状态：pending-待支付, success-成功, failed-失败, refund-已退款")
    private String paymentStatus; // 支付状态

    @ApiModelProperty(value = "第三方交易ID")
    private String transactionId; // 第三方交易ID

    @ApiModelProperty(value = "支付完成时间")
    private LocalDateTime paidTime; // 支付完成时间

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount; // 退款金额

    @ApiModelProperty(value = "备注")
    private String remark; // 备注

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
