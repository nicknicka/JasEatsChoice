package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群订单实体
 */
@Data
@TableName("t_group_order")
public class GroupOrder {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 群订单发起者ID
     */
    private String initiatorId;

    /**
     * 商家ID
     */
    private String merchantId;

    /**
     * 群ID
     */
    private String groupId;

    /**
     * 地址ID
     */
    private String addressId;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 订单总金额
     */
    private Double totalAmount;

    /**
     * 订单状态：-1-草稿、0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成、6-已取消
     */
    private Integer status;

    /**
     * 最大参与人数
     */
    private Integer maxParticipants;

    /**
     * 是否锁单
     */
    private Boolean locked;

    /**
     * 确认成团时间
     */
    private LocalDateTime confirmedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
