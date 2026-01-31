package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Role;
import com.xx.jaseatschoicejava.service.RoleService;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/api/admin/settings/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取角色列表
     */
    @ApiOperation("获取角色列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:list')")
    public ResponseEntity<IPage<Role>> getRoleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Role> pageParam = new Page<>(page, pageSize);
        IPage<Role> result = roleService.page(pageParam);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有角色（不分页，用于下拉选择）
     */
    @ApiOperation("获取所有角色")
    @GetMapping("/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.list();
        return ResponseEntity.ok(roles);
    }

    /**
     * 创建角色
     */
    @ApiOperation("创建角色")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:create')")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Role role) {
        boolean success = roleService.save(role);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "创建成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "创建失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新角色
     */
    @ApiOperation("更新角色")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:create')")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long roleId,
            @RequestBody Role role) {

        role.setRoleId(roleId);
        boolean success = roleService.updateById(role);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "更新失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除角色
     */
    @ApiOperation("删除角色")
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:create')")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long roleId) {
        boolean success = roleService.removeById(roleId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "删除失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取角色的权限ID列表
     */
    @ApiOperation("获取角色权限")
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<Map<String, Object>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = roleService.getPermissionIdsByRoleId(roleId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("permissionIds", permissionIds);
        return ResponseEntity.ok(response);
    }

    /**
     * 分配权限给角色
     */
    @ApiOperation("分配权限")
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<Integer> permissionIds = (List<Integer>) request.get("permissionIds");

        boolean success = roleService.assignPermissions(roleId, permissionIds);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "分配成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "分配失败");
            return ResponseEntity.status(500).body(response);
        }
    }
}
