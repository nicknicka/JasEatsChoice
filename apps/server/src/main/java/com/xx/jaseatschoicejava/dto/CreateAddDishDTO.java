package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 创建加菜请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "创建加菜请求DTO")
public class CreateAddDishDTO {

    @ApiModelProperty(value = "群订单ID", required = true)
    private String groupOrderId;

    @ApiModelProperty(value = "原订单ID(可选,已支付订单才需要)")
    private String originalOrderId;

    @ApiModelProperty(value = "加菜菜品列表", required = true)
    private List<DishItem> dishItems;

    /**
     * 菜品项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DishItem {
        @ApiModelProperty(value = "菜品ID", required = true)
        private String dishId;

        @ApiModelProperty(value = "菜品数量", required = true)
        private Integer quantity;

        @ApiModelProperty(value = "定制要求")
        private String customization;
    }
}
