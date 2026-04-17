package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加菜请求实体
 */
@Data
@TableName("t_add_dish_request")
public class AddDishRequest {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 群订单ID
     */
    private String groupOrderId;

    /**
     * 原订单ID(关联已支付订单)
     */
    private String originalOrderId;

    /**
     * 加菜请求人ID
     */
    private String requestUserId;

    /**
     * 商家ID
     */
    private String merchantId;

    /**
     * 加菜菜品信息JSON
     * 格式: [{"dishId":"2016329138809384963","quantity":2,"customization":"微辣","price":28.00}]
     */
    private String dishInfo;

    /**
     * 加菜总金额
     */
    private BigDecimal totalAmount;

    /**
     * 审核状态: 0-待审核,1-审核通过,2-审核驳回,3-已撤回,4-超时驳回
     */
    private Integer approvalStatus;

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 审核人ID(群订单发起者)
     */
    private String reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 首次提醒时间(10分钟)
     */
    private LocalDateTime firstRemindTime;

    /**
     * 二次提醒时间
     */
    private LocalDateTime secondRemindTime;

    /**
     * 超时时间(15分钟)
     */
    private LocalDateTime timeoutTime;

    /**
     * 关联支付记录ID
     */
    private String relatedPaymentId;

    /**
     * 关联订单ID(审核通过后创建)
     */
    private String relatedOrderId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
