package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("system_config")
@ApiModel(description = "系统配置实体")
public class SystemConfig {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "配置ID")
    private String id;

    @ApiModelProperty(value = "配置键")
    private String configKey;

    @ApiModelProperty(value = "配置值")
    private String configValue;

    @ApiModelProperty(value = "配置名称")
    private String configName;

    @ApiModelProperty(value = "配置分组")
    private String configGroup; // 如: system, payment, sms, email, ai等

    @ApiModelProperty(value = "配置类型：string-字符串, number-数字, boolean-布尔, json-JSON对象")
    private String configType; // string, number, boolean, json

    @ApiModelProperty(value = "配置描述")
    private String description;

    @ApiModelProperty(value = "是否系统内置：true-系统内置不可删除, false-自定义可删除")
    private Boolean isSystem;

    @ApiModelProperty(value = "状态：active-启用, inactive-禁用")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
