package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单菜品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_dish")
public class OrderDish {

    @TableId
    private Long id; // 订单菜品ID

    private Long orderId; // 订单ID

    private Long dishId; // 菜品ID

    private Integer quantity; // 数量

    private BigDecimal price; // 单价

    private String customization; // 定制要求
}