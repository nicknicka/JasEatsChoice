package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_permission")
@ApiModel(description = "权限实体")
public class Permission {

    @TableId(value = "permission_id", type = IdType.AUTO)
    @ApiModelProperty(value = "权限ID")
    private Long permissionId;

    @TableField("permission_name")
    @ApiModelProperty(value = "权限名称")
    private String permissionName;

    @TableField("permission_code")
    @ApiModelProperty(value = "权限编码")
    private String permissionCode;

    @TableField("resource_type")
    @ApiModelProperty(value = "资源类型：MENU-菜单, BUTTON-按钮, API-接口")
    private String resourceType;

    @TableField("parent_id")
    @ApiModelProperty(value = "父权限ID（0表示顶级权限）")
    private Long parentId;

    @TableField("path")
    @ApiModelProperty(value = "路由路径（菜单类型使用）")
    private String path;

    @TableField("icon")
    @ApiModelProperty(value = "图标")
    private String icon;

    @TableField("description")
    @ApiModelProperty(value = "权限描述")
    private String description;

    @TableField("sort_order")
    @ApiModelProperty(value = "排序序号")
    private Integer sortOrder;

    @TableField("status")
    @ApiModelProperty(value = "状态：ACTIVE-启用, DISABLED-禁用")
    private String status;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "子权限列表（非数据库字段，树形结构使用）")
    private java.util.List<Permission> children;
}
