package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_consume_history")
public class ConsumeHistory {

    @TableId(type = IdType.ASSIGN_ID)
    private String id; // 记录ID

    private String userId; // 用户ID

    private String type; // 类型：recharge（充值）、consume（消费）、withdraw（提现）

    private BigDecimal amount; // 金额

    private String description; // 描述

    private String status; // 状态：success（成功）、failed（失败）

    private LocalDateTime createTime; // 创建时间
}
