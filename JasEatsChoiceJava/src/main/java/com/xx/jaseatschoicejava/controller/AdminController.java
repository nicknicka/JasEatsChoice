package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Admin;
import com.xx.jaseatschoicejava.service.AdminService;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员管理控制器
 */
@Api(tags = "管理员管理")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @ApiOperation("管理员登录")
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        String token = adminService.login(username, password);

        Map<String, Object> response = new HashMap<>();
        if (token != null) {
            response.put("success", true);
            response.put("token", token);
            response.put("message", "登录成功");

            // 获取管理员信息
            Admin admin = adminService.getByUsernameWithRole(username);
            if (admin != null) {
                Map<String, Object> adminInfo = new HashMap<>();
                adminInfo.put("adminId", admin.getAdminId());
                adminInfo.put("username", admin.getUsername());
                adminInfo.put("realName", admin.getRealName());
                adminInfo.put("avatar", admin.getAvatar());
                adminInfo.put("roleCode", admin.getRoleCode());
                adminInfo.put("roleName", admin.getRoleName());
                response.put("admin", adminInfo);
            }
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * 获取管理员列表（分页）
     */
    @ApiOperation("获取管理员列表")
    @GetMapping("/list")
    public ResponseEntity<IPage<Admin>> getAdminList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("用户名") @RequestParam(required = false) String username,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("角色ID") @RequestParam(required = false) Long roleId) {

        Page<Admin> pageParam = new Page<>(page, pageSize);
        IPage<Admin> result = adminService.getAdminPage(pageParam, username, status, roleId);

        return ResponseEntity.ok(result);
    }

    /**
     * 创建管理员
     */
    @ApiOperation("创建管理员")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:create')")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Admin admin) {
        // 从JWT中获取当前管理员ID
        Long operatorId = AdminContext.getAdminId();

        boolean success = adminService.createAdmin(admin, operatorId);

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
     * 修改管理员状态
     */
    @ApiOperation("修改管理员状态")
    @PutMapping("/{adminId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long adminId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        // 从JWT中获取当前管理员ID
        Long operatorId = AdminContext.getAdminId();

        boolean success = adminService.updateStatus(adminId, status, operatorId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "状态修改成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "状态修改失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 重置管理员密码
     */
    @ApiOperation("重置管理员密码")
    @PutMapping("/{adminId}/password")
    @PreAuthorize("hasAnyAuthority('admin:setting:role:create')")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long adminId,
            @RequestBody Map<String, String> request) {

        String newPassword = request.get("password");
        // 从JWT中获取当前管理员ID
        Long operatorId = AdminContext.getAdminId();

        boolean success = adminService.resetPassword(adminId, newPassword, operatorId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "密码重置成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "密码重置失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取当前登录管理员信息
     */
    @ApiOperation("获取当前管理员信息")
    @GetMapping(value = "/current", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, Object>> getCurrentAdmin() {
        Map<String, Object> response = new HashMap<>();

        // 从JWT中获取当前管理员信息
        Long adminId = AdminContext.getAdminId();
        String username = AdminContext.getAdminUsername();

        if (adminId != null) {
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                // 如果通过ID查不到，尝试通过用户名查询
                admin = adminService.getByUsernameWithRole(username);
            }

            if (admin != null) {
                // 重新查询以获取角色信息
                admin = adminService.getByUsernameWithRole(admin.getUsername());
            }

            if (admin != null) {
                Map<String, Object> adminInfo = new HashMap<>();
                adminInfo.put("adminId", admin.getAdminId());
                adminInfo.put("username", admin.getUsername() != null ? admin.getUsername() : "");
                adminInfo.put("realName", admin.getRealName() != null ? admin.getRealName() : "管理员");
                adminInfo.put("avatar", admin.getAvatar() != null ? admin.getAvatar() : "");
                adminInfo.put("roleCode", admin.getRoleCode() != null ? admin.getRoleCode() : "");
                adminInfo.put("roleName", admin.getRoleName() != null ? admin.getRoleName() : "超级管理员");
                response.put("success", true);
                response.put("admin", adminInfo);
                return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
            }
        }

        response.put("success", false);
        response.put("message", "未找到管理员信息");
        return ResponseEntity.status(404)
            .header("Content-Type", "application/json;charset=UTF-8")
            .body(response);
    }
}
