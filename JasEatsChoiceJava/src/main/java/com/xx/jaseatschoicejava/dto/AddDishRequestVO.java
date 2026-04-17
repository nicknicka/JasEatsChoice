package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 加菜请求VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "加菜请求VO")
public class AddDishRequestVO {

    @ApiModelProperty(value = "加菜请求ID")
    private String id;

    @ApiModelProperty(value = "群订单ID")
    private String groupOrderId;

    @ApiModelProperty(value = "请求用户信息")
    private UserInfo requestUserInfo;

    @ApiModelProperty(value = "加菜菜品信息")
    private List<DishInfo> dishInfo;

    @ApiModelProperty(value = "加菜总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "提交时间")
    private LocalDateTime submitTime;

    @ApiModelProperty(value = "是否有饮食禁忌冲突")
    private Boolean allergyConflict;

    @ApiModelProperty(value = "冲突详情")
    private List<String> conflictDetails;

    @ApiModelProperty(value = "审核状态: 0-待审核,1-通过,2-驳回,3-撤回,4-超时驳回")
    private Integer status;

    @ApiModelProperty(value = "状态描述")
    private String statusDesc;

    @ApiModelProperty(value = "剩余时间(秒)")
    private Long remainingTime;

    @ApiModelProperty(value = "驳回原因")
    private String rejectReason;

    /**
     * 用户信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @ApiModelProperty(value = "用户ID")
        private String userId;

        @ApiModelProperty(value = "昵称")
        private String nickname;

        @ApiModelProperty(value = "头像URL")
        private String avatar;
    }

    /**
     * 菜品信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DishInfo {
        @ApiModelProperty(value = "菜品ID")
        private String dishId;

        @ApiModelProperty(value = "菜品名称")
        private String dishName;

        @ApiModelProperty(value = "菜品数量")
        private Integer quantity;

        @ApiModelProperty(value = "菜品单价")
        private BigDecimal price;

        @ApiModelProperty(value = "定制要求")
        private String customization;
    }
}
