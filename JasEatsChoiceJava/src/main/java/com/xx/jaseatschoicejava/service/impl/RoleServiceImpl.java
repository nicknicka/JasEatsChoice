package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Role;
import com.xx.jaseatschoicejava.entity.RolePermission;
import com.xx.jaseatschoicejava.mapper.RoleMapper;
import com.xx.jaseatschoicejava.mapper.RolePermissionMapper;
import com.xx.jaseatschoicejava.service.RoleService;
import com.xx.jaseatschoicejava.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public boolean assignPermissions(Long roleId, List<Integer> permissionIds) {
        // 先删除原有权限
        rolePermissionService.lambdaUpdate()
                .eq(RolePermission::getRoleId, roleId)
                .remove();

        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Integer permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId.longValue());
                rolePermissionService.save(rolePermission);
            }
        }

        return true;
    }
}
