package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼单成员实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_group_order_member")
@ApiModel(description = "拼单成员实体")
public class GroupOrderMember {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @TableField("group_order_id")
    @ApiModelProperty(value = "拼单ID")
    private String groupOrderId;

    @TableField("user_id")
    @ApiModelProperty(value = "用户ID")
    private String userId;

    @TableField("role")
    @ApiModelProperty(value = "成员角色：initiator-发起人，member-普通成员")
    private String role;

    @TableField("join_time")
    @ApiModelProperty(value = "加入时间")
    private LocalDateTime joinTime;

    @TableField("pay_status")
    @ApiModelProperty(value = "支付状态：pending-待支付，paid-已支付，refund-已退款")
    private String payStatus;

    @TableField("paid_amount")
    @ApiModelProperty(value = "累计实付金额")
    private BigDecimal paidAmount;

    @TableField("leave_status")
    @ApiModelProperty(value = "离开状态：0-在拼单中，1-已退出")
    private Integer leaveStatus;

    @TableField("leave_time")
    @ApiModelProperty(value = "退出时间")
    private LocalDateTime leaveTime;

    @TableField("invite_by")
    @ApiModelProperty(value = "邀请人用户ID")
    private String inviteBy;

    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
