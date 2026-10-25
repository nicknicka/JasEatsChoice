package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
@ApiModel(description = "用户实体")
public class User {
    @TableId(value = "user_id", type = com.baomidou.mybatisplus.annotation.IdType.INPUT)
    @ApiModelProperty(value = "用户ID（当前存储为16位数字字符串）")
    private String userId; // 用户ID（当前存储为16位数字字符串）

    @TableField("phone")
    @ApiModelProperty(value = "手机号码（登录账号）")
    private String phone; // 手机号码（登录账号）

    @TableField("password")
    @ApiModelProperty(value = "密码")
    @JsonIgnore
    private String password; // 密码

    @TableField("payment_password")
    @ApiModelProperty(value = "支付密码（加密存储）")
    @JsonIgnore
    private String paymentPassword; // 支付密码（加密存储）

    @TableField("has_payment_password")
    @ApiModelProperty(value = "是否已设置支付密码：0-未设置，1-已设置")
    private Boolean hasPaymentPassword; // 是否已设置支付密码

    @TableField("nickname")
    @ApiModelProperty(value = "昵称")
    private String nickname; // 昵称

    @TableField("height")
    @ApiModelProperty(value = "身高（单位：cm）")
    private Double height; // 身高（单位：cm）

    @TableField("weight")
    @ApiModelProperty(value = "体重（单位：kg）")
    private Double weight; // 体重（单位：kg）

    @TableField("diet_goal")
    @ApiModelProperty(value = "饮食目标（如：减肥、增肌、保持健康）")
    private String dietGoal; // 饮食目标（如：减肥、增肌、保持健康）

    @TableField("allergies")
    @ApiModelProperty(value = "过敏食材列表（JSON格式）")
    private JsonNode allergies; // 过敏食材列表（JSON格式）

    @TableField("prefer_tags")
    @ApiModelProperty(value = "偏好标签列表（JSON格式）")
    private JsonNode preferTags; // 偏好标签列表（JSON格式）

    @TableField("email")
    @ApiModelProperty(value = "邮箱地址")
    private String email; // 邮箱地址

    @TableField("disable_weather_recommend")
    @ApiModelProperty(value = "是否关闭天气推荐：true-关闭，false-开启")
    private Boolean disableWeatherRecommend; // 是否关闭天气推荐：true-关闭，false-开启

    @TableField("merchant_id")
    @ApiModelProperty(value = "商家ID")
    private String merchantId; // 商家ID，如果不为空表示用户已注册为商家

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间

    @TableField("avatar")
    @ApiModelProperty(value = "用户头像URL")
    private String avatar; // 用户头像URL

    @TableField("location")
    @ApiModelProperty(value = "用户所在地")
    private String location; // 用户所在地

    @TableField("gender")
    @ApiModelProperty(value = "性别：male-男，female-女，other-其他")
    private String gender; // 性别

    @TableField("birthday")
    @ApiModelProperty(value = "生日")
    private String birthday; // 生日

    @TableField("bio")
    @ApiModelProperty(value = "个人简介")
    private String bio; // 个人简介
}
