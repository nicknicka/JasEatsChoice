package com.xx.jaseatschoicejava.vo;

import com.xx.jaseatschoicejava.entity.Admin;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员列表视图：只暴露管理端列表需要的字段，
 * 明确不带 password / salt 等认证凭据。
 */
@Data
public class AdminVO {

    private Long adminId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String status;
    private Long roleId;
    private String roleName;
    private String roleCode;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createBy;
    private Long updateBy;
    private String remark;

    public static AdminVO from(Admin admin) {
        AdminVO vo = new AdminVO();
        vo.adminId = admin.getAdminId();
        vo.username = admin.getUsername();
        vo.realName = admin.getRealName();
        vo.phone = admin.getPhone();
        vo.email = admin.getEmail();
        vo.avatar = admin.getAvatar();
        vo.status = admin.getStatus();
        vo.roleId = admin.getRoleId();
        vo.roleName = admin.getRoleName();
        vo.roleCode = admin.getRoleCode();
        vo.lastLoginTime = admin.getLastLoginTime();
        vo.lastLoginIp = admin.getLastLoginIp();
        vo.createdAt = admin.getCreatedAt();
        vo.updatedAt = admin.getUpdatedAt();
        vo.createBy = admin.getCreateBy();
        vo.updateBy = admin.getUpdateBy();
        vo.remark = admin.getRemark();
        return vo;
    }
}
