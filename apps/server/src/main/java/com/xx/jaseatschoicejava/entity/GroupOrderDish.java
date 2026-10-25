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
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 群订单ID
     */
    private String groupOrderId;

    /**
     * 菜品ID
     */
    private String dishId;

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
    private String userId;
}
