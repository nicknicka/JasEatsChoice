package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@TableName("t_banner")
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 类型: link-外部链接, dish-菜品, merchant-商家, activity-活动
     */
    private String type;

    /**
     * 跳转目标类型
     */
    private String targetType;

    /**
     * 跳转目标ID
     */
    private String targetId;

    /**
     * 外部链接
     */
    private String link;

    /**
     * 位置: home-首页, merchant-商家页, dish-菜品页
     */
    private String position;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
