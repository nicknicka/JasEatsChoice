package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加菜设置实体
 */
@Data
@TableName("t_add_dish_setting")
public class AddDishSetting {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 群订单ID
     */
    private String groupOrderId;

    /**
     * 加菜权限: 0-全员可加菜,1-仅发起者可加菜
     */
    private Integer addDishPermission;

    /**
     * 单次加菜预算限制(可选)
     */
    private BigDecimal budgetLimit;

    /**
     * 单次加菜数量限制(可选)
     */
    private Integer maxDishCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
