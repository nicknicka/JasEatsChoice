package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 查询所有顶级权限
     */
    List<Permission> getTopLevelPermissions();

    /**
     * 根据父级ID查询子权限列表
     */
    List<Permission> getPermissionsByParentId(Long parentId);

    /**
     * 查询权限树（递归）
     */
    List<Permission> getPermissionTree();

    /**
     * 根据角色ID查询权限编码列表
     */
    List<String> getPermissionCodesByRoleId(Long roleId);

    /**
     * 根据管理员ID查询所有权限编码
     */
    List<String> getPermissionCodesByAdminId(Long adminId);

    /**
     * 分页查询权限列表
     */
    IPage<Permission> getPermissionPage(Page<Permission> page, String permissionName, String resourceType, String status);
}
