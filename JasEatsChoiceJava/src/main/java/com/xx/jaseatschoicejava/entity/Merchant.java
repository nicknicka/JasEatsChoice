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

    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.INPUT)
    @ApiModelProperty(value = "商家ID")
    private String id; // 商家ID (M + 16位数字)

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

    @TableField("password")
    @ApiModelProperty(value = "密码")
    private String password; // 密码

    @TableField("email")
    @ApiModelProperty(value = "邮箱")
    private String email; // 邮箱

    @TableField("business_license")
    @ApiModelProperty(value = "营业执照号")
    private String businessLicense; // 营业执照号

    @TableField(value = "business_scope", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @ApiModelProperty(value = "经营范围")
    private JsonNode businessScope; // 经营范围

    @TableField("contact_name")
    @ApiModelProperty(value = "联系人姓名")
    private String contactName; // 联系人姓名
    @TableField("avatar")
    private String avatar; // 商家头像

    @TableField("rating")
    @ApiModelProperty(value = "商家评分")
    private BigDecimal rating; // 商家评分

    @TableField(value = "business_hours", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @ApiModelProperty(value = "营业时间")
    private JsonNode businessHours; // 营业时间

    @TableField("average_price")
    @ApiModelProperty(value = "人均消费")
    private BigDecimal averagePrice; // 人均消费

    @TableField("status")
    @ApiModelProperty(value = "状态：true-营业，false-休息")
    private Boolean status; // 状态：true-营业，false-休息

    @TableField("audit_status")
    @ApiModelProperty(value = "审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝")
    private String auditStatus; // 审核状态

    @TableField("audit_reason")
    @ApiModelProperty(value = "审核备注/原因")
    private String auditReason; // 审核备注/原因

    @TableField("audit_time")
    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditTime; // 审核时间

    @TableField("audit_by")
    @ApiModelProperty(value = "审核人ID")
    private String auditBy; // 审核人ID

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间

    @TableField(value = "album", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @ApiModelProperty(value = "店铺相册（JSON格式，包含environment和dishes两个数组）")
    private JsonNode album; // 店铺相册（JSON格式，包含environment和dishes两个数组）

}
