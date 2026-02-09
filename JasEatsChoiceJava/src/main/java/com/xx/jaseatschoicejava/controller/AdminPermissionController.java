package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Permission;
import com.xx.jaseatschoicejava.service.PermissionService;
import com.xx.jaseatschoicejava.service.SystemLogService;
import com.xx.jaseatschoicejava.util.AdminContext;
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
 * 管理员-权限管理控制器
 */
@Api(tags = "管理员-权限管理")
@RestController
@RequestMapping("/admin/permissions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired(required = false)
    private SystemLogService systemLogService;

    /**
     * 分页查询权限列表
     */
    @ApiOperation("分页查询权限列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<IPage<Permission>> getPermissionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String permissionName,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String status) {

        Page<Permission> pageParam = new Page<>(page, pageSize);
        IPage<Permission> result = permissionService.getPermissionPage(pageParam, permissionName, resourceType, status);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取权限树
     */
    @ApiOperation("获取权限树")
    @GetMapping("/tree")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> getPermissionTree() {
        List<Permission> tree = permissionService.getPermissionTree();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", tree);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取顶级权限
     */
    @ApiOperation("获取顶级权限")
    @GetMapping("/top")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> getTopLevelPermissions() {
        List<Permission> permissions = permissionService.getTopLevelPermissions();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", permissions);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据父级ID获取子权限
     */
    @ApiOperation("根据父级ID获取子权限")
    @GetMapping("/children/{parentId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> getChildPermissions(@PathVariable Long parentId) {
        List<Permission> permissions = permissionService.getPermissionsByParentId(parentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", permissions);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取权限详情
     */
    @ApiOperation("获取权限详情")
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> getPermissionDetail(@PathVariable Long permissionId) {
        Permission permission = permissionService.getById(permissionId);

        Map<String, Object> response = new HashMap<>();
        if (permission != null) {
            response.put("success", true);
            response.put("data", permission);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "权限不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 创建权限
     */
    @ApiOperation("创建权限")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> createPermission(@RequestBody Map<String, Object> request) {
        String permissionName = (String) request.get("permissionName");
        String permissionCode = (String) request.get("permissionCode");
        String resourceType = (String) request.get("resourceType");
        String path = (String) request.get("path");
        Long parentId = request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : 0L;
        String icon = (String) request.get("icon");
        String description = (String) request.get("description");
        Integer sortOrder = (Integer) request.get("sortOrder");

        // 检查权限编码是否已存在
        long count = permissionService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Permission>()
                .eq("permission_code", permissionCode)
        );

        if (count > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限编码已存在");
            return ResponseEntity.status(400).body(response);
        }

        Permission permission = new Permission();
        permission.setPermissionName(permissionName);
        permission.setPermissionCode(permissionCode);
        permission.setResourceType(resourceType);
        permission.setPath(path);
        permission.setParentId(parentId);
        permission.setIcon(icon);
        permission.setDescription(description);
        permission.setStatus("ACTIVE");
        permission.setSortOrder(sortOrder != null ? sortOrder : 0);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());

        boolean success = permissionService.save(permission);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "CREATE", "PERMISSION", "创建权限：" + permissionName,
                adminId, adminName, "ADMIN",
                "AdminPermissionController.createPermission",
                "permissionName=" + permissionName + ", permissionCode=" + permissionCode,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "权限创建成功");
            response.put("data", permission);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "权限创建失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新权限
     */
    @ApiOperation("更新权限")
    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody Map<String, Object> request) {

        Permission permission = permissionService.getById(permissionId);
        if (permission == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不存在");
            return ResponseEntity.status(404).body(response);
        }

        String permissionName = (String) request.get("permissionName");
        String path = (String) request.get("path");
        Long parentId = request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null;
        String icon = (String) request.get("icon");
        String description = (String) request.get("description");
        String status = (String) request.get("status");
        Integer sortOrder = (Integer) request.get("sortOrder");

        if (permissionName != null) permission.setPermissionName(permissionName);
        if (path != null) permission.setPath(path);
        if (parentId != null) permission.setParentId(parentId);
        if (icon != null) permission.setIcon(icon);
        if (description != null) permission.setDescription(description);
        if (status != null) permission.setStatus(status);
        if (sortOrder != null) permission.setSortOrder(sortOrder);
        permission.setUpdateTime(LocalDateTime.now());

        boolean success = permissionService.updateById(permission);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "UPDATE", "PERMISSION", "更新权限：" + permissionName,
                adminId, adminName, "ADMIN",
                "AdminPermissionController.updatePermission",
                "permissionId=" + permissionId,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "权限更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "权限更新失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除权限
     */
    @ApiOperation("删除权限")
    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:permission')")
    public ResponseEntity<Map<String, Object>> deletePermission(@PathVariable Long permissionId) {
        Permission permission = permissionService.getById(permissionId);
        if (permission == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "权限不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 检查是否有子权限
        long childCount = permissionService.count(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Permission>()
                .eq("parent_id", permissionId)
        );

        if (childCount > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "该权限下有子权限，无法删除");
            return ResponseEntity.status(400).body(response);
        }

        boolean success = permissionService.removeById(permissionId);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "DELETE", "PERMISSION", "删除权限：" + permission.getPermissionName(),
                adminId, adminName, "ADMIN",
                "AdminPermissionController.deletePermission",
                "permissionId=" + permissionId,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "权限删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "权限删除失败");
            return ResponseEntity.status(500).body(response);
        }
    }
}
