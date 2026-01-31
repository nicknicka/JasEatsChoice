package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜品步骤历史记录实体
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@TableName("t_dish_step_history")
public class DishStepHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 订单菜品ID（关联t_order_dish表）
     */
    private String orderDishId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 原步骤状态
     */
    private Integer oldStepStatus;

    /**
     * 新步骤状态
     */
    private Integer newStepStatus;

    /**
     * 操作类型：FORWARD-前进, BACKWARD-回退, SKIP-跳过
     */
    private String operationType;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 回退原因（回退操作必填）
     */
    private String rollbackReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 预计完成时间（分钟）
     */
    private Integer estimatedMinutes;
}
