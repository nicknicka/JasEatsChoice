package com.xx.jaseatschoicejava.agent.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情DTO
 *
 * 用于Agent工具类返回订单详细信息
 *
 * @author Claude
 * @since 2026-03-24
 */
@Data
@Builder
public class OrderDetail {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商家ID
     */
    private String merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 订单状态（0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
     */
    private Integer status;

    /**
     * 订单状态文本
     */
    private String statusText;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;

    /**
     * 就餐信息（堂食/自取）
     * 堂食示例：堂食 - 座号：A12
     * 自取示例：自取
     */
    private String address;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 订单项列表
     */
    private List<OrderItemDTO> items;

    /**
     * 是否找到订单
     */
    private Boolean found;
}
