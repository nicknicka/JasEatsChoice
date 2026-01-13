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

/**
 * 联系人关系实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_contact")
@ApiModel(description = "联系人关系实体")
public class Contact {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "联系人ID")
    private String id; // 联系人ID

    @ApiModelProperty(value = "用户ID")
    private String userId; // 用户ID

    @ApiModelProperty(value = "目标ID（好友ID或群ID）")
    private String targetId; // 目标ID（好友ID或群ID）

    @ApiModelProperty(value = "关系类型：friend(好友)、group(群成员)")
    private String relationType; // 关系类型：friend(好友)、group(群成员)

    @ApiModelProperty(value = "关系状态：normal(正常)、pending(待验证)、blocked(已拉黑)")
    private String status; // 关系状态：normal(正常)、pending(待验证)、blocked(已拉黑)

    @ApiModelProperty(value = "群角色：creator(创建者)、admin(管理员)、member(普通成员)")
    private String role; // 群角色：creator(创建者)、admin(管理员)、member(普通成员)

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime; // 更新时间
}
