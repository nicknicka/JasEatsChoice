package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 营养数据库实体（中国食物成分表）
 * 基于《中国食物成分表 标准版（第6版）》
 *
 * @author Claude
 * @since 2026-03-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_nutrition")
@ApiModel(description = "营养数据实体")
public class Nutrition {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;

    @ApiModelProperty(value = "食物编码")
    private String foodCode;

    @ApiModelProperty(value = "食物名称")
    private String foodName;

    @ApiModelProperty(value = "可食部(%)")
    private BigDecimal edible;

    // 能量
    @ApiModelProperty(value = "能量(千卡)")
    private BigDecimal energyKcal;

    @ApiModelProperty(value = "能量(千焦)")
    private BigDecimal energyKj;

    // 主要营养素
    @ApiModelProperty(value = "水分(g)")
    private BigDecimal water;

    @ApiModelProperty(value = "蛋白质(g)")
    private BigDecimal protein;

    @ApiModelProperty(value = "脂肪(g)")
    private BigDecimal fat;

    @ApiModelProperty(value = "碳水化合物(g)")
    private BigDecimal cho;

    @ApiModelProperty(value = "膳食纤维(g)")
    private BigDecimal dietaryFiber;

    // 其他营养素
    @ApiModelProperty(value = "胆固醇(mg)")
    private BigDecimal cholesterol;

    @ApiModelProperty(value = "灰分(g)")
    private BigDecimal ash;

    // 维生素
    @ApiModelProperty(value = "维生素A(μgRE)")
    private BigDecimal vitaminA;

    @ApiModelProperty(value = "胡萝卜素(μg)")
    private BigDecimal carotene;

    @ApiModelProperty(value = "视黄醇(μg)")
    private BigDecimal retinol;

    @ApiModelProperty(value = "硫胺素(mg)")
    private BigDecimal thiamin;

    @ApiModelProperty(value = "核黄素(mg)")
    private BigDecimal riboflavin;

    @ApiModelProperty(value = "烟酸(mg)")
    private BigDecimal niacin;

    @ApiModelProperty(value = "维生素C(mg)")
    private BigDecimal vitaminC;

    @ApiModelProperty(value = "维生素E总(mg)")
    private BigDecimal vitaminETotal;

    @ApiModelProperty(value = "α-维生素E(mg)")
    private BigDecimal vitaminE1;

    @ApiModelProperty(value = "β+γ-维生素E(mg)")
    private BigDecimal vitaminE2;

    @ApiModelProperty(value = "δ-维生素E(mg)")
    private BigDecimal vitaminE3;

    // 矿物质
    @ApiModelProperty(value = "钙(mg)")
    private BigDecimal ca;

    @ApiModelProperty(value = "磷(mg)")
    private BigDecimal p;

    @ApiModelProperty(value = "钾(mg)")
    private BigDecimal k;

    @ApiModelProperty(value = "钠(mg)")
    private BigDecimal na;

    @ApiModelProperty(value = "镁(mg)")
    private BigDecimal mg;

    @ApiModelProperty(value = "铁(mg)")
    private BigDecimal fe;

    @ApiModelProperty(value = "锌(mg)")
    private BigDecimal zn;

    @ApiModelProperty(value = "硒(μg)")
    private BigDecimal se;

    @ApiModelProperty(value = "铜(mg)")
    private BigDecimal cu;

    @ApiModelProperty(value = "锰(mg)")
    private BigDecimal mn;

    // 元数据
    @ApiModelProperty(value = "备注说明")
    private String remark;

    @ApiModelProperty(value = "数据来源")
    private String dataSource;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
