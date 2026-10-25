package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐拒绝行为实体
 */
@Data
@TableName("t_reject_recommendation")
public class RejectRecommendation {
    /**
     * 主键ID
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 菜品ID
     */
    private String dishId;

    /**
     * 拒绝时间
     */
    private LocalDateTime rejectTime;

    /**
     * 拒绝原因
     */
    private String reason;
}
