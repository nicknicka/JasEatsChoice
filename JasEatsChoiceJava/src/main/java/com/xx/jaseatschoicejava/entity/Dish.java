package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
// import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 菜品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_dish")
@ApiModel(description = "菜品实体")
public class Dish {

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    @ApiModelProperty(value = "菜品ID")
    private String id; // 菜品ID

    @TableField("merchant_id")
    @ApiModelProperty(value = "商家ID")
    private String merchantId; // 商家ID

    @TableField("name")
    @ApiModelProperty(value = "菜品名称")
    private String name; // 菜品名称

    @TableField("category")
    @ApiModelProperty(value = "菜品分类")
    private String category; // 菜品分类

    @TableField("price")
    @ApiModelProperty(value = "菜品价格")
    private BigDecimal price; // 菜品价格

    @TableField("calorie")
    @ApiModelProperty(value = "卡路里含量")
    private Integer calorie; // 卡路里含量

    @TableField("ingredients")
    @ApiModelProperty(value = "食材列表（JSON格式）")
    private String ingredients; // 食材列表（JSON格式）

    @TableField("description")
    @ApiModelProperty(value = "菜品描述")
    private String description; // 菜品描述

    @TableField("image")
    @ApiModelProperty(value = "菜品图片URL")
    private String image; // 菜品图片URL

    @TableField("score")
    @ApiModelProperty(value = "推荐得分")
    private BigDecimal score; // 推荐得分

    @TableField("avg_rating")
    @ApiModelProperty(value = "平均评分")
    private BigDecimal avgRating; // 平均评分

    @TableField("status")
    @ApiModelProperty(value = "状态：true-上架，false-下架")
    private Boolean status; // 状态：true-上架，false-下架

    @TableField("stock")
    @ApiModelProperty(value = "库存数量")
    private Integer stock; // 库存数量

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}