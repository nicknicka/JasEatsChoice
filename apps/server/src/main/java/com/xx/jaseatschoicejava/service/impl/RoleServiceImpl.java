package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Role;
import com.xx.jaseatschoicejava.entity.Permission;
import com.xx.jaseatschoicejava.entity.RolePermissionRelation;
import com.xx.jaseatschoicejava.entity.AdminRoleRelation;
import com.xx.jaseatschoicejava.mapper.RoleMapper;
import com.xx.jaseatschoicejava.mapper.PermissionMapper;
import com.xx.jaseatschoicejava.mapper.RolePermissionRelationMapper;
import com.xx.jaseatschoicejava.mapper.AdminRoleRelationMapper;
import com.xx.jaseatschoicejava.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionRelationMapper rolePermissionRelationMapper;

    @Autowired
    private AdminRoleRelationMapper adminRoleRelationMapper;

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return roleMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    @Transactional
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有权限
        rolePermissionRelationMapper.deleteByRoleId(roleId);

        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermissionRelation> relations = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                RolePermissionRelation relation = new RolePermissionRelation();
                relation.setRoleId(roleId);
                relation.setPermissionId(permissionId);
                relation.setCreateTime(LocalDateTime.now());
                relations.add(relation);
            }
            // 批量插入
            for (RolePermissionRelation relation : relations) {
                rolePermissionRelationMapper.insert(relation);
            }
        }

        return true;
    }

    @Override
    public List<Role> getRolesByAdminId(Long adminId) {
        return roleMapper.selectRolesByAdminId(adminId);
    }

    @Override
    @Transactional
    public boolean assignRolesToAdmin(Long adminId, List<Long> roleIds) {
        // 先删除原有角色
        adminRoleRelationMapper.deleteByAdminId(adminId);

        // 添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                AdminRoleRelation relation = new AdminRoleRelation();
                relation.setAdminId(adminId);
                relation.setRoleId(roleId);
                relation.setCreateTime(LocalDateTime.now());
                adminRoleRelationMapper.insert(relation);
            }
        }

        return true;
    }

    @Override
    public IPage<Role> getRolePage(Page<Role> page, String roleName, String status) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();

        if (roleName != null && !roleName.isEmpty()) {
            queryWrapper.like("role_name", roleName);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByAsc("sort_order");

        return roleMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Integer countPermissionsByRoleId(Long roleId) {
        return roleMapper.countPermissionsByRoleId(roleId);
    }
}
