package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 食谱实体
 */
@Data
@TableName("t_recipe")
@ApiModel(description = "食谱实体")
public class Recipe {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "食谱ID")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "食谱名称")
    private String name;

    @ApiModelProperty(value = "食谱类型: breakfast/lunch/dinner/snack")
    private String type;

    @ApiModelProperty(value = "食谱菜品列表")
    @TableField(value = "items")
    private String items;

    @ApiModelProperty(value = "卡路里")
    private Integer calories;

    @ApiModelProperty(value = "蛋白质")
    private Integer protein;

    @ApiModelProperty(value = "碳水化合物")
    private Integer carbs;

    @ApiModelProperty(value = "脂肪")
    private Integer fat;

    @ApiModelProperty(value = "自定义营养信息（JSON格式）")
    @TableField("custom_nutrition")
    private String customNutrition;

    @ApiModelProperty(value = "食谱详情")
    private String detail;

    @ApiModelProperty(value = "烹饪时间")
    private String cookTime;

    @ApiModelProperty(value = "是否收藏")
    @TableField("is_favorite")
    private Boolean favorite;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
