package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节日推荐菜品关联实体类
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
@TableName("t_festival_dish_recommend")
public class FestivalDishRecommend {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 节日ID
     */
    private String festivalId;

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 推荐类型
     */
    private String recommendType;

    /**
     * 推荐理由
     */
    private String recommendReason;

    /**
     * 展示位置（0-首页, 1-列表顶, 2-banner）
     */
    private Integer position;

    /**
     * 优先级（数字越大越靠前）
     */
    private Integer priority;

    /**
     * 点击次数统计
     */
    private Integer clickCount;

    /**
     * 订单次数统计
     */
    private Integer orderCount;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 菜品名称（非数据库字段）
     */
    @TableField(exist = false)
    private String dishName;

    /**
     * 菜品图片（非数据库字段）
     */
    @TableField(exist = false)
    private String dishImage;

    /**
     * 菜品价格（非数据库字段）
     */
    @TableField(exist = false)
    private java.math.BigDecimal dishPrice;
}
