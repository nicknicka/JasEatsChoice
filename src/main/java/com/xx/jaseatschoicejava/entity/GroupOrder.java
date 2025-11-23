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
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群订单发起者ID
     */
    private Long initiatorId;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 地址ID
     */
    private Long addressId;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 订单状态：0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成、6-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
