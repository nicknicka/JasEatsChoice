package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 群订单加菜实体
 */
@Data
@TableName("t_group_order_addition")
public class GroupOrderAddition {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 群订单ID
     */
    private String groupOrderId;

    /**
     * 群组ID
     */
    private String groupId;

    /**
     * 加菜用户ID
     */
    private String userId;

    /**
     * 加菜用户姓名
     */
    private String userName;

    /**
     * 加菜菜品列表（JSON格式）
     */
    private String dishes;

    /**
     * 加菜总额
     */
    private BigDecimal totalAmount;

    /**
     * 状态：pending_review(待审核)/approved_pending_payment(审核通过待支付)/rejected(已驳回)/paid(已支付)
     */
    private String status;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 饮食禁忌检查结果（JSON格式）
     */
    private String checkDietRestrictions;

    /**
     * 关联的加菜订单ID
     */
    private String relatedOrderId;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
