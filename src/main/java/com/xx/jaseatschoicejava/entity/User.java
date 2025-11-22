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

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
@ApiModel(description = "用户实体")
public class User {

    @TableId
    @TableField("id")
    @ApiModelProperty(value = "用户ID")
    private Long id; // 用户ID

    @TableField("phone")
    @ApiModelProperty(value = "手机号码（登录账号）")
    private String phone; // 手机号码（登录账号）

    @TableField("password")
    @ApiModelProperty(value = "密码")
    private String password; // 密码

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

    @TableField("disable_weather_recommend")
    @ApiModelProperty(value = "是否关闭天气推荐：true-关闭，false-开启")
    private Boolean disableWeatherRecommend; // 是否关闭天气推荐：true-关闭，false-开启

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
