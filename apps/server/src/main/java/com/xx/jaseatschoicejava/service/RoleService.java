package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Role;
import com.xx.jaseatschoicejava.entity.Permission;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据角色ID查询权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);

    /**
     * 给角色分配权限
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 根据管理员ID查询角色列表
     */
    List<Role> getRolesByAdminId(Long adminId);

    /**
     * 给管理员分配角色
     */
    boolean assignRolesToAdmin(Long adminId, List<Long> roleIds);

    /**
     * 分页查询角色列表
     */
    IPage<Role> getRolePage(Page<Role> page, String roleName, String status);

    /**
     * 根据角色ID查询权限数量
     */
    Integer countPermissionsByRoleId(Long roleId);
}
