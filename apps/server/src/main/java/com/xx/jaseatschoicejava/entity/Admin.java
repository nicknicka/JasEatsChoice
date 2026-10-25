package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_admin")
@ApiModel(description = "管理员实体")
public class Admin {

    @TableId(value = "admin_id", type = IdType.AUTO)
    @ApiModelProperty(value = "管理员ID")
    private Long adminId;

    @TableField("username")
    @ApiModelProperty(value = "管理员用户名")
    private String username;

    @TableField("password")
    @ApiModelProperty(value = "密码（加密）")
    private String password;

    @TableField("real_name")
    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @TableField("phone")
    @ApiModelProperty(value = "手机号")
    private String phone;

    @TableField("email")
    @ApiModelProperty(value = "邮箱")
    private String email;

    @TableField("avatar")
    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @TableField("status")
    @ApiModelProperty(value = "状态：ACTIVE-活跃, LOCKED-锁定, DELETED-删除")
    private String status;

    @TableField("role_id")
    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @TableField("last_login_time")
    @ApiModelProperty(value = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @TableField("last_login_ip")
    @ApiModelProperty(value = "最后登录IP")
    private String lastLoginIp;

    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @TableField("create_by")
    @ApiModelProperty(value = "创建人ID")
    private Long createBy;

    @TableField("update_by")
    @ApiModelProperty(value = "更新人ID")
    private Long updateBy;

    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    @TableField(exist = false)
    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @TableField(exist = false)
    @ApiModelProperty(value = "角色编码")
    private String roleCode;
}
