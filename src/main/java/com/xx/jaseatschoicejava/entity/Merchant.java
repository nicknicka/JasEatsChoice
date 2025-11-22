package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 商家实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_merchant")
@ApiModel(description = "商家实体")
public class Merchant {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "商家ID")
    private Long id; // 商家ID

    @TableField("name")
    @ApiModelProperty(value = "商家名称")
    private String name; // 商家名称

    @TableField("address")
    @ApiModelProperty(value = "商家地址")
    private String address; // 商家地址

    @TableField("longitude")
    @ApiModelProperty(value = "经度")
    private BigDecimal longitude; // 经度

    @TableField("latitude")
    @ApiModelProperty(value = "纬度")
    private BigDecimal latitude; // 纬度

    @TableField("category")
    @ApiModelProperty(value = "商家分类")
    private String category; // 商家分类

    @TableField("phone")
    @ApiModelProperty(value = "联系电话")
    private String phone; // 联系电话

    @TableField("business_hours")
    @ApiModelProperty(value = "营业时间（JSON格式）")
    private JsonNode businessHours; // 营业时间（JSON格式）

    @TableField("average_price")
    @ApiModelProperty(value = "人均消费")
    private BigDecimal averagePrice; // 人均消费

    @TableField("status")
    @ApiModelProperty(value = "状态：true-营业，false-休息")
    private Boolean status; // 状态：true-营业，false-休息

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
