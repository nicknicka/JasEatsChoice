package com.xx.jaseatschoicejava.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 想吃列表项详情VO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "想吃列表项详情")
public class WishListItemDetailVO {

    @ApiModelProperty(value = "列表项ID")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "商家ID")
    private String merchantId;

    @ApiModelProperty(value = "商家名称")
    private String merchantName;

    @ApiModelProperty(value = "菜品名称")
    private String dishName;

    @ApiModelProperty(value = "菜品图片")
    private String dishImage;

    @ApiModelProperty(value = "口味要求")
    private String tasteRequirement;

    @ApiModelProperty(value = "详细描述")
    private String description;

    @ApiModelProperty(value = "参考食谱ID")
    private String recipeId;

    @ApiModelProperty(value = "食谱名称")
    private String recipeName;

    @ApiModelProperty(value = "审核状态")
    private Integer auditStatus;

    @ApiModelProperty(value = "审核状态名称")
    private String auditStatusName;

    @ApiModelProperty(value = "拒绝原因代码")
    private Integer rejectionReasonCode;

    @ApiModelProperty(value = "拒绝原因标题")
    private String rejectionReasonTitle;

    @ApiModelProperty(value = "拒绝原因描述")
    private String rejectionReasonDescription;

    @ApiModelProperty(value = "商家审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "审核人姓名")
    private String auditorName;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "是否已申诉")
    private Boolean isAppealed;

    @ApiModelProperty(value = "申诉内容")
    private String appealContent;

    @ApiModelProperty(value = "申诉时间")
    private LocalDateTime appealTime;

    @ApiModelProperty(value = "申诉回复")
    private String appealReply;

    @ApiModelProperty(value = "申诉回复时间")
    private LocalDateTime appealReplyTime;

    @ApiModelProperty(value = "期望上架时间")
    private LocalDateTime expectedAvailableTime;

    @ApiModelProperty(value = "实际上架时间")
    private String actualAvailableTime;

    @ApiModelProperty(value = "超时时间")
    private LocalDateTime timeoutTime;

    @ApiModelProperty(value = "剩余审核时间（小时）")
    private Long remainingHours;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "是否可申诉")
    private Boolean canAppeal;

    @ApiModelProperty(value = "是否可撤回")
    private Boolean canWithdraw;
}
