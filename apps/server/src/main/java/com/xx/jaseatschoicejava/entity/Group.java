package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_group")
@ApiModel(description = "群信息实体")
public class Group {

    @TableId(type = IdType.INPUT)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "群ID")
    private String id; // 群ID (G + 16位数字)

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "创建者ID")
    private String creatorId; // 创建者ID

    @ApiModelProperty(value = "群名称")
    private String groupName; // 群名称

    @ApiModelProperty(value = "群描述")
    private String description; // 群描述

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
