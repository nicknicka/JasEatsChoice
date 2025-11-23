package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 群订单菜品实体
 */
@Data
@TableName("t_group_order_dish")
public class GroupOrderDish {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群订单ID
     */
    private Long groupOrderId;

    /**
     * 菜品ID
     */
    private Long dishId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 定制要求
     */
    private String customization;

    /**
     * 用户ID
     */
    private Long userId;
}
