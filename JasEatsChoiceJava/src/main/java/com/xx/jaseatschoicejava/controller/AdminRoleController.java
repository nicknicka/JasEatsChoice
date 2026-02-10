package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Permission;
import com.xx.jaseatschoicejava.entity.Role;
import com.xx.jaseatschoicejava.service.RoleService;
import com.xx.jaseatschoicejava.util.AdminContext;
import com.xx.jaseatschoicejava.util.SystemLogHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-角色管理控制器
 */
@Api(tags = "管理员-角色管理")
@RestController
@RequestMapping("/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色列表
     */
    @ApiOperation("分页查询角色列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:list')")
    public ResponseEntity<IPage<Role>> getRoleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String status) {

        Page<Role> pageParam = new Page<>(page, pageSize);
        IPage<Role> result = roleService.getRolePage(pageParam, roleName, status);

        // 为每个角色添加权限数量
        result.getRecords().forEach(role -> {
            Integer permissionCount = roleService.countPermissionsByRoleId(role.getRoleId());
            role.setPermissionCount(permissionCount);
        });

        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有角色（不分页，用于下拉选择）
     */
    @ApiOperation("获取所有角色")
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:list')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role>()
                .eq("status", "ACTIVE")
                .orderByAsc("sort_order")
        );
        return ResponseEntity.ok(roles);
    }

    /**
     * 获取角色详情
     */
    @ApiOperation("获取角色详情")
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:detail')")
    public ResponseEntity<Map<String, Object>> getRoleDetail(@PathVariable Long roleId) {
        Role role = roleService.getById(roleId);

        Map<String, Object> response = new HashMap<>();
        if (role != null) {
            // 获取角色的权限列表
            List<Permission> permissions = roleService.getPermissionsByRoleId(roleId);
            List<Long> permissionIds = roleService.getPermissionIdsByRoleId(roleId);

            response.put("success", true);
            response.put("data", role);
            response.put("permissions", permissions);
            response.put("permissionIds", permissionIds);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "角色不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 创建角色
     */
    @ApiOperation("创建角色")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:create')")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Map<String, Object> request) {
        String roleName = (String) request.get("roleName");
        String roleCode = (String) request.get("roleCode");
        String description = (String) request.get("description");
        Integer sortOrder = (Integer) request.get("sortOrder");

        // 检查角色编码是否已存在
        long count = roleService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Role>()
                .eq("role_code", roleCode)
        );

        if (count > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "角色编码已存在");
            return ResponseEntity.status(400).body(response);
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setDescription(description);
        role.setStatus("ACTIVE");
        role.setSortOrder(sortOrder != null ? sortOrder : 0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());

        boolean success = roleService.save(role);

        // 记录操作日志
        if (success) {
            SystemLogHelper.logCreate(
                "角色管理",
                "创建角色：" + roleName,
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("roleName", roleName, "roleCode", roleCode, "description", description)
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "角色创建成功");
            response.put("data", role);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "角色创建失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新角色
     */
    @ApiOperation("更新角色")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:update')")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long roleId,
            @RequestBody Map<String, Object> request) {

        Role role = roleService.getById(roleId);
        if (role == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "角色不存在");
            return ResponseEntity.status(404).body(response);
        }

        String roleName = (String) request.get("roleName");
        String description = (String) request.get("description");
        String status = (String) request.get("status");
        Integer sortOrder = (Integer) request.get("sortOrder");

        if (roleName != null) role.setRoleName(roleName);
        if (description != null) role.setDescription(description);
        if (status != null) role.setStatus(status);
        if (sortOrder != null) role.setSortOrder(sortOrder);
        role.setUpdateTime(LocalDateTime.now());

        boolean success = roleService.updateById(role);

        // 记录操作日志
        if (success) {
            SystemLogHelper.logUpdate(
                "角色管理",
                "更新角色：" + roleName,
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("roleId", roleId, "roleName", roleName)
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "角色更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "角色更新失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除角色
     */
    @ApiOperation("删除角色")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:delete')")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long roleId) {
        Role role = roleService.getById(roleId);
        if (role == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "角色不存在");
            return ResponseEntity.status(404).body(response);
        }

        boolean success = roleService.removeById(roleId);

        // 记录操作日志
        if (success) {
            SystemLogHelper.logDelete(
                "角色管理",
                "删除角色：" + role.getRoleName(),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("roleId", roleId, "roleName", role.getRoleName())
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "角色删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "角色删除失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 给角色分配权限
     */
    @ApiOperation("给角色分配权限")
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) request.get("permissionIds");

        boolean success = roleService.assignPermissions(roleId, permissionIds);

        // 记录操作日志
        if (success) {
            Role role = roleService.getById(roleId);
            SystemLogHelper.logUpdate(
                "角色管理",
                "为角色【" + role.getRoleName() + "】分配" + permissionIds.size() + "个权限",
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("roleId", roleId, "permissionCount", permissionIds.size())
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "权限分配成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "权限分配失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取角色的权限列表
     */
    @ApiOperation("获取角色的权限列表")
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:detail')")
    public ResponseEntity<Map<String, Object>> getRolePermissions(@PathVariable Long roleId) {
        List<Permission> permissions = roleService.getPermissionsByRoleId(roleId);
        List<Long> permissionIds = roleService.getPermissionIdsByRoleId(roleId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", permissions);
        response.put("permissionIds", permissionIds);
        return ResponseEntity.ok(response);
    }
}
