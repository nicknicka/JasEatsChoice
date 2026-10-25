package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色-权限关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_role_permission_relation")
@ApiModel(description = "角色-权限关联实体")
public class RolePermissionRelation {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "关联ID")
    private Long relationId;

    @TableField("role_id")
    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @TableField("permission_id")
    @ApiModelProperty(value = "权限ID")
    private Long permissionId;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
