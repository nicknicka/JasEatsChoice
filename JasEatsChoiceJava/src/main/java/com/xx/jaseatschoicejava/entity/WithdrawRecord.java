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
 * 提现记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_withdraw_record")
@ApiModel(description = "提现记录实体")
public class WithdrawRecord {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "提现记录ID")
    private Long id; // 提现记录ID

    @ApiModelProperty(value = "提现流水号")
    private String withdrawNo; // 提现流水号

    @ApiModelProperty(value = "用户ID")
    private Long userId; // 用户ID

    @ApiModelProperty(value = "提现金额")
    private BigDecimal amount; // 提现金额

    @ApiModelProperty(value = "手续费")
    private BigDecimal fee; // 手续费

    @ApiModelProperty(value = "实际到账金额")
    private BigDecimal actualAmount; // 实际到账金额

    @ApiModelProperty(value = "提现方式：wechat-微信, alipay-支付宝, bank-银行卡")
    private String withdrawMethod; // 提现方式

    @ApiModelProperty(value = "提现账号信息（脱敏）")
    private String accountInfo; // 提现账号信息

    @ApiModelProperty(value = "提现状态：pending-待审核, approved-已通过, rejected-已拒绝, processing-处理中, success-成功, failed-失败")
    private String withdrawStatus; // 提现状态

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime; // 审核时间

    @ApiModelProperty(value = "审核人")
    private String auditUser; // 审核人

    @ApiModelProperty(value = "完成时间")
    private LocalDateTime completeTime; // 完成时间

    @ApiModelProperty(value = "拒绝原因")
    private String rejectReason; // 拒绝原因

    @ApiModelProperty(value = "备注")
    private String remark; // 备注

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
