package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户评价实体类
 */
@Data
@TableName("t_review")
public class Review {

    /**
     * 评价ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商家ID
     */
    private String merchantId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 菜品ID（可为空，表示对整个商家的评价）
     */
    private String dishId;

    /**
     * 评价星级（1-5）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片URL列表（JSON格式）
     */
    private String images;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 评价状态（0-正常，1-已删除）
     */
    private Integer status;
}
