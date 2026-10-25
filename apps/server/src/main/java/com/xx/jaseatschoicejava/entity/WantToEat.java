package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 想吃的菜品实体类
 */
@Data
@TableName("t_want_to_eat")
public class WantToEat {
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
    private String itemId;

    /**
     * 审核状态：pending-待审核, approved-已通过, rejected-已拒绝
     */
    private String status;

    /**
     * 审核备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
