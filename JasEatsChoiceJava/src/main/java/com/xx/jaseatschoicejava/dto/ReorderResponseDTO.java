package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 再来一单响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "再来一单响应")
public class ReorderResponseDTO {

    @ApiModelProperty(value = "原订单ID")
    private String originalOrderId;

    @ApiModelProperty(value = "商家ID")
    private String merchantId;

    @ApiModelProperty(value = "商家名称")
    private String merchantName;

    @ApiModelProperty(value = "原订单总金额")
    private BigDecimal originalTotalAmount;

    @ApiModelProperty(value = "当前订单总金额")
    private BigDecimal currentTotalAmount;

    @ApiModelProperty(value = "金额变动说明")
    private String amountChangeNote;

    @ApiModelProperty(value = "原订单备注")
    private String originalRemark;

    @ApiModelProperty(value = "原订单地址ID")
    private String originalAddressId;

    @ApiModelProperty(value = "原订单地址")
    private String originalAddress;

    @ApiModelProperty(value = "菜品列表")
    private List<ReorderItemDTO> items;

    @ApiModelProperty(value = "是否有菜品变动")
    private Boolean hasChanges;

    @ApiModelProperty(value = "下架菜品数量")
    private Integer soldOutCount;

    @ApiModelProperty(value = "涨价菜品数量")
    private Integer priceIncreasedCount;

    @ApiModelProperty(value = "降价菜品数量")
    private Integer priceDecreasedCount;

    @ApiModelProperty(value = "正常菜品数量")
    private Integer normalCount;

    @ApiModelProperty(value = "是否所有菜品都不可用")
    private Boolean allItemsUnavailable;
}
