package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 食物营养成分实体（1346条食物数据）
 * 来源于food_nutrition.sql
 *
 * @author Claude
 * @since 2026-03-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("j_food_nutrition")
@ApiModel(description = "食物营养成分实体")
public class JFoodNutrition {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "分类ID")
    private Integer cateId;

    @ApiModelProperty(value = "食物名称")
    private String foodName;

    @ApiModelProperty(value = "别名或俗名")
    private String aliasName;

    @ApiModelProperty(value = "英文名称")
    private String englishName;

    @ApiModelProperty(value = "食部(可食部分比例%)")
    private String ediblePart;

    @ApiModelProperty(value = "水分")
    private String water;

    @ApiModelProperty(value = "能量")
    private String energy;

    @ApiModelProperty(value = "蛋白质")
    private String protein;

    @ApiModelProperty(value = "脂肪")
    private String fat;

    @ApiModelProperty(value = "胆固醇")
    private String cholesterol;

    @ApiModelProperty(value = "灰分")
    private String ash;

    @ApiModelProperty(value = "碳水化合物")
    private String carbohydrate;

    @ApiModelProperty(value = "总膳食纤维")
    private String dietaryFiber;

    @ApiModelProperty(value = "胡萝卜素")
    private String carotene;

    @ApiModelProperty(value = "维生素A")
    private String vitaminA;

    @ApiModelProperty(value = "α-维生素E")
    private String vitaminE;

    @ApiModelProperty(value = "硫胺素")
    private String thiamin;

    @ApiModelProperty(value = "核黄素")
    private String riboflavin;

    @ApiModelProperty(value = "烟酸")
    private String niacin;

    @ApiModelProperty(value = "维生素C")
    private String vitaminC;

    @ApiModelProperty(value = "钙")
    private String calcium;

    @ApiModelProperty(value = "磷")
    private String phosphorus;

    @ApiModelProperty(value = "钾")
    private String potassium;

    @ApiModelProperty(value = "钠")
    private String sodium;

    @ApiModelProperty(value = "镁")
    private String magnesium;

    @ApiModelProperty(value = "铁")
    private String iron;

    @ApiModelProperty(value = "锌")
    private String zinc;

    @ApiModelProperty(value = "硒")
    private String selenium;

    @ApiModelProperty(value = "铜")
    private String copper;

    @ApiModelProperty(value = "锰")
    private String manganese;

    @ApiModelProperty(value = "碘")
    private String iodine;

    @ApiModelProperty(value = "饱和脂肪酸")
    private String sfa;

    @ApiModelProperty(value = "单不饱和脂肪酸")
    private String mufa;

    @ApiModelProperty(value = "多不饱和脂肪酸")
    private String pufa;

    @ApiModelProperty(value = "脂肪酸合计")
    private String fattyAcidsTotal;
}
