package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 加菜设置DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "加菜设置DTO")
public class AddDishSettingDTO {

    @ApiModelProperty(value = "群订单ID")
    private String groupOrderId;

    @ApiModelProperty(value = "加菜权限: 0-全员可加菜,1-仅发起者可加菜")
    private Integer addDishPermission;

    @ApiModelProperty(value = "单次加菜预算限制(可选)")
    private BigDecimal budgetLimit;

    @ApiModelProperty(value = "单次加菜数量限制(可选)")
    private Integer maxDishCount;

    @ApiModelProperty(value = "当前已加菜次数(仅查询时返回)")
    private Integer currentAddCount;
}
