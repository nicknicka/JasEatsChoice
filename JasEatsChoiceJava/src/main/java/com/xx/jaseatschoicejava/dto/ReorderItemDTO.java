package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 再来一单菜品项DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "再来一单菜品项")
public class ReorderItemDTO {

    @ApiModelProperty(value = "订单菜品ID")
    private String orderDishId;

    @ApiModelProperty(value = "菜品ID")
    private String dishId;

    @ApiModelProperty(value = "菜品名称")
    private String dishName;

    @ApiModelProperty(value = "菜品图片")
    private String dishImage;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "原订单价格")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "当前价格")
    private BigDecimal currentPrice;

    @ApiModelProperty(value = "价格变动说明")
    private String priceChangeNote;

    @ApiModelProperty(value = "是否涨价")
    private Boolean isPriceIncreased;

    @ApiModelProperty(value = "价格涨幅（百分比）")
    private BigDecimal priceIncreaseRate;

    @ApiModelProperty(value = "菜品状态：0-正常, 1-已下架, 2-库存不足")
    private Integer dishStatus;

    @ApiModelProperty(value = "状态描述：normal/sold_out/out_of_stock")
    private String statusDescription;

    @ApiModelProperty(value = "推荐替换菜品ID（当原菜品下架时）")
    private String suggestedDishId;

    @ApiModelProperty(value = "推荐替换菜品名称")
    private String suggestedDishName;

    @ApiModelProperty(value = "推荐替换菜品价格")
    private BigDecimal suggestedDishPrice;

    @ApiModelProperty(value = "推荐替换菜品图片")
    private String suggestedDishImage;

    @ApiModelProperty(value = "推荐替换原因")
    private String suggestionReason;

    @ApiModelProperty(value = "定制要求")
    private String customization;

    @ApiModelProperty(value = "是否可以选择（用户决定是否加入购物车）")
    private Boolean canSelect;

    @ApiModelProperty(value = "默认选中状态")
    private Boolean defaultSelected;
}
