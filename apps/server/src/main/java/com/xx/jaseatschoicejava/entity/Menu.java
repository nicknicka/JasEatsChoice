package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 菜单实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_menu")
@ApiModel(description = "菜单实体")
public class Menu {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "菜单ID")
    private String id; // 菜单ID

    @TableField("merchant_id")
    @ApiModelProperty(value = "商家ID")
    private String merchantId; // 商家ID

    @TableField("name")
    @ApiModelProperty(value = "菜单名称")
    @JsonProperty("menuName") // 前端使用的字段名是menuName
    private String name; // 菜单名称

    @TableField("type")
    @ApiModelProperty(value = "菜单类型（如：早餐、午餐、晚餐）")
    @JsonProperty("category") // 前端使用的字段名是category
    private String type; // 菜单类型（如：早餐、午餐、晚餐）

    @TableField("description")
    @ApiModelProperty(value = "菜单描述")
    private String description; // 菜单描述

    @TableField("status")
    @ApiModelProperty(value = "菜单状态：active-启用，inactive-禁用")
    private String status; // 菜单状态：active-启用，inactive-禁用

    @TableField("auto_start_time")
    @ApiModelProperty(value = "自动开始时间")
    @JsonProperty("autoOnline") // 前端使用的字段名是autoOnline
    private LocalDateTime autoStartTime; // 自动开始时间

    @TableField("auto_end_time")
    @ApiModelProperty(value = "自动结束时间")
    @JsonProperty("autoOffline") // 前端使用的字段名是autoOffline
    private LocalDateTime autoEndTime; // 自动结束时间

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间

    @TableField(exist = false)
    @ApiModelProperty(value = "菜品数量")
    private Integer dishCount; // 菜品数量（数据库中不存在，用于返回前端）
}
