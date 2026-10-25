package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_refund_record")
@ApiModel(description = "退款记录实体")
public class RefundRecord {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "退款记录ID")
    private String refundId;

    @ApiModelProperty(value = "退款流水号")
    private String refundNo;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款原因：QUALITY-商品质量问题, DONT_WANT-不想要了, DELAY-配送延迟, OTHER-其他")
    private String reason;

    @ApiModelProperty(value = "退款原因描述")
    private String reasonDescription;

    @ApiModelProperty(value = "退款状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, PROCESSING-处理中, SUCCESS-成功, FAILED-失败")
    private String status;

    @ApiModelProperty(value = "申请时间")
    private LocalDateTime applyTime;

    @ApiModelProperty(value = "处理时间")
    private LocalDateTime processTime;

    @ApiModelProperty(value = "审核人ID")
    private Long auditBy;

    @ApiModelProperty(value = "审核人名称")
    private String auditByName;

    @ApiModelProperty(value = "处理备注")
    private String processComment;

    @ApiModelProperty(value = "退款方式：ORIGINAL-原路退回, BALANCE-退回余额")
    private String refundMethod;

    @ApiModelProperty(value = "第三方退款流水号")
    private String transactionId;

    @ApiModelProperty(value = "完成时间")
    private LocalDateTime completeTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
