package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 想吃列表项实体
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@TableName("t_wish_list_item")
@ApiModel(description = "想吃列表项")
public class WishListItem {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "商家ID（可选，指定商家的需求）")
    private String merchantId;

    @ApiModelProperty(value = "菜品名称")
    private String dishName;

    @ApiModelProperty(value = "菜品图片URL")
    private String dishImage;

    @ApiModelProperty(value = "口味要求")
    private String tasteRequirement;

    @ApiModelProperty(value = "详细描述")
    private String description;

    @ApiModelProperty(value = "参考食谱ID（关联用户的食谱）")
    private String recipeId;

    @ApiModelProperty(value = "审核状态：0-待审核, 1-已通过, 2-已拒绝, 3-申诉中, 4-申诉成功, 5-申诉失败, 6-超时自动通过, 7-已撤回")
    private Integer auditStatus;

    @ApiModelProperty(value = "拒绝原因代码")
    private Integer rejectionReasonCode;

    @ApiModelProperty(value = "拒绝原因说明")
    private String rejectionReason;

    @ApiModelProperty(value = "商家审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "审核人ID")
    private String auditorId;

    @ApiModelProperty(value = "审核人姓名")
    private String auditorName;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "是否申诉")
    private Boolean isAppealed;

    @ApiModelProperty(value = "申诉内容")
    private String appealContent;

    @ApiModelProperty(value = "申诉时间")
    private LocalDateTime appealTime;

    @ApiModelProperty(value = "申诉回复")
    private String appealReply;

    @ApiModelProperty(value = "申诉回复时间")
    private LocalDateTime appealReplyTime;

    @ApiModelProperty(value = "申诉回复人ID")
    private String appealReplierId;

    @ApiModelProperty(value = "期望上架时间")
    private LocalDateTime expectedAvailableTime;

    @ApiModelProperty(value = "实际上架时间（审核通过后商家填写的预计时间）")
    private String actualAvailableTime;

    @ApiModelProperty(value = "超时时间（24小时自动通过）")
    private LocalDateTime timeoutTime;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
