package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单菜品批量查询DTO
 */
@Data
@NoArgsConstructor
@ApiModel(description = "订单菜品批量查询DTO")
public class OrderDishBatchQueryDTO {

    @ApiModelProperty(value = "订单ID列表")
    private List<String> orderIds;
}
