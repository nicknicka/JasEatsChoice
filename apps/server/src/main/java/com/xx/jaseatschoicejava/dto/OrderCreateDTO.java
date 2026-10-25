package com.xx.jaseatschoicejava.dto;

import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 创建订单DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "创建订单请求对象")
public class OrderCreateDTO {

    @ApiModelProperty(value = "订单信息")
    private Order order;

    @ApiModelProperty(value = "订单菜品列表")
    private List<OrderDish> dishes;
}
