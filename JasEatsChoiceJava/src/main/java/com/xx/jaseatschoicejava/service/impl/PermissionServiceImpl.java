package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Permission;
import com.xx.jaseatschoicejava.mapper.PermissionMapper;
import com.xx.jaseatschoicejava.mapper.RoleMapper;
import com.xx.jaseatschoicejava.mapper.AdminRoleRelationMapper;
import com.xx.jaseatschoicejava.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AdminRoleRelationMapper adminRoleRelationMapper;

    @Override
    public List<Permission> getTopLevelPermissions() {
        return permissionMapper.selectTopLevelPermissions();
    }

    @Override
    public List<Permission> getPermissionsByParentId(Long parentId) {
        return permissionMapper.selectByParentId(parentId);
    }

    @Override
    public List<Permission> getPermissionTree() {
        // 查询所有权限
        List<Permission> allPermissions = permissionMapper.selectList(
            new QueryWrapper<Permission>().eq("status", "ACTIVE").orderByAsc("sort_order")
        );

        // 构建树形结构
        return buildTree(allPermissions, 0L);
    }

    /**
     * 递归构建权限树
     */
    private List<Permission> buildTree(List<Permission> permissions, Long parentId) {
        List<Permission> tree = new ArrayList<>();

        for (Permission permission : permissions) {
            // 判断是否为指定父级的子节点
            boolean isChild = (parentId == 0L && (permission.getParentId() == null || permission.getParentId() == 0L))
                           || (parentId != 0L && parentId.equals(permission.getParentId()));

            if (isChild) {
                // 递归查找子节点
                List<Permission> children = buildTree(permissions, permission.getPermissionId());
                if (!children.isEmpty()) {
                    permission.setChildren(children);
                }
                tree.add(permission);
            }
        }

        return tree;
    }

    @Override
    public List<String> getPermissionCodesByRoleId(Long roleId) {
        return permissionMapper.selectPermissionCodesByRoleId(roleId);
    }

    @Override
    public List<String> getPermissionCodesByAdminId(Long adminId) {
        // 先获取管理员的角色列表
        List<Long> roleIds = adminRoleRelationMapper.selectRoleIdsByAdminId(adminId);

        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色的权限编码（去重）
        return roleIds.stream()
            .flatMap(roleId -> permissionMapper.selectPermissionCodesByRoleId(roleId).stream())
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public IPage<Permission> getPermissionPage(Page<Permission> page, String permissionName, String resourceType, String status) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();

        if (permissionName != null && !permissionName.isEmpty()) {
            queryWrapper.like("permission_name", permissionName);
        }

        if (resourceType != null && !resourceType.isEmpty()) {
            queryWrapper.eq("resource_type", resourceType);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByAsc("sort_order");

        return permissionMapper.selectPage(page, queryWrapper);
    }
}
