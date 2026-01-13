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
 * 充值记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_recharge_record")
@ApiModel(description = "充值记录实体")
public class RechargeRecord {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "充值记录ID")
    private Long id; // 充值记录ID

    @ApiModelProperty(value = "充值流水号")
    private String rechargeNo; // 充值流水号

    @ApiModelProperty(value = "用户ID")
    private Long userId; // 用户ID

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amount; // 充值金额

    @ApiModelProperty(value = "支付方式：wechat-微信, alipay-支付宝, bank-银行卡")
    private String paymentMethod; // 支付方式

    @ApiModelProperty(value = "充值状态：pending-待支付, success-成功, failed-失败")
    private String rechargeStatus; // 充值状态

    @ApiModelProperty(value = "第三方交易ID")
    private String transactionId; // 第三方交易ID

    @ApiModelProperty(value = "支付完成时间")
    private LocalDateTime paidTime; // 支付完成时间

    @ApiModelProperty(value = "备注")
    private String remark; // 备注

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
