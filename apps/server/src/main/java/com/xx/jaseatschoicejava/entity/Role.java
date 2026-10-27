package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
@ApiModel(description = "角色实体")
public class Role {

    @TableId(value = "role_id", type = IdType.AUTO)
    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @TableField("role_name")
    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @TableField("role_code")
    @ApiModelProperty(value = "角色编码：SUPER_ADMIN-超级管理员, ADMIN-普通管理员, AUDITOR-审核员")
    private String roleCode;

    @TableField("description")
    @ApiModelProperty(value = "角色描述")
    private String description;

    @TableField("status")
    @ApiModelProperty(value = "状态：ACTIVE-启用, DISABLED-禁用")
    private String status;

    @TableField("created_at")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    @TableField("sort_order")
    @ApiModelProperty(value = "排序序号")
    private Integer sortOrder;

    @TableField(exist = false)
    @ApiModelProperty(value = "权限数量（非数据库字段）")
    private Integer permissionCount;
}
