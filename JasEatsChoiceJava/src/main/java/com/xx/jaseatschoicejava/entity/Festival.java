package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 节日实体类
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
@TableName("t_festival")
public class Festival {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 节日名称
     */
    private String festivalName;

    /**
     * 节日类型
     */
    private String festivalType;

    /**
     * 节日日期（MM-dd格式）
     */
    private String festivalDate;

    /**
     * 年份（NULL表示每年重复）
     */
    private Integer year;

    /**
     * 节日描述
     */
    private String description;

    /**
     * 节日图标
     */
    private String icon;

    /**
     * 背景图片
     */
    private String backgroundImage;

    /**
     * 主题颜色
     */
    private String themeColor;

    /**
     * 开始日期（用于季节性）
     */
    private LocalDate startDate;

    /**
     * 结束日期（用于季节性）
     */
    private LocalDate endDate;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否当前生效（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean isCurrent;

    /**
     * 距离节日天数（非数据库字段）
     */
    @TableField(exist = false)
    private Integer daysUntilFestival;
}
