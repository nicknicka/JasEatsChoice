package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员-角色关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_admin_role_relation")
@ApiModel(description = "管理员-角色关联实体")
public class AdminRoleRelation {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "关联ID")
    private Long relationId;

    @TableField("admin_id")
    @ApiModelProperty(value = "管理员ID")
    private Long adminId;

    @TableField("role_id")
    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
