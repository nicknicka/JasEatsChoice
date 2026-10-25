package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.entity.Admin;
import com.xx.jaseatschoicejava.service.AdminService;
import com.xx.jaseatschoicejava.util.AdminContext;
import com.xx.jaseatschoicejava.util.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员管理控制器
 */
@Api(tags = "管理员管理")
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private FileUploadConfig fileUploadConfig;

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
                adminInfo.put("username", admin.getUsername() != null ? admin.getUsername() : "");
                adminInfo.put("realName", admin.getRealName() != null ? admin.getRealName() : "");
                adminInfo.put("phone", admin.getPhone() != null ? admin.getPhone() : "");
                adminInfo.put("email", admin.getEmail() != null ? admin.getEmail() : "");
                adminInfo.put("avatar", admin.getAvatar() != null ? admin.getAvatar() : "");
                adminInfo.put("roleCode", admin.getRoleCode() != null ? admin.getRoleCode() : "");
                adminInfo.put("roleName", admin.getRoleName() != null ? admin.getRoleName() : "");
                adminInfo.put("status", admin.getStatus() != null ? admin.getStatus() : "ACTIVE");
                adminInfo.put("createTime", admin.getCreateTime() != null ? admin.getCreateTime().toString() : "");
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
                adminInfo.put("phone", admin.getPhone() != null ? admin.getPhone() : "");
                adminInfo.put("email", admin.getEmail() != null ? admin.getEmail() : "");
                adminInfo.put("avatar", admin.getAvatar() != null ? admin.getAvatar() : "");
                adminInfo.put("roleCode", admin.getRoleCode() != null ? admin.getRoleCode() : "");
                adminInfo.put("roleName", admin.getRoleName() != null ? admin.getRoleName() : "超级管理员");
                adminInfo.put("status", admin.getStatus() != null ? admin.getStatus() : "ACTIVE");
                adminInfo.put("createTime", admin.getCreateTime() != null ? admin.getCreateTime().toString() : "");
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

    /**
     * 更新当前管理员个人信息
     */
    @ApiOperation("更新当前管理员个人信息")
    @PutMapping(value = "/profile", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> updateData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 从JWT中获取当前管理员信息
            Long adminId = AdminContext.getAdminId();

            if (adminId == null) {
                response.put("success", false);
                response.put("message", "管理员信息不存在");
                return ResponseEntity.status(401).body(response);
            }

            // 获取管理员信息
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                response.put("success", false);
                response.put("message", "管理员不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 更新基本信息
            if (updateData.containsKey("realName")) {
                admin.setRealName((String) updateData.get("realName"));
            }
            if (updateData.containsKey("phone")) {
                admin.setPhone((String) updateData.get("phone"));
            }
            if (updateData.containsKey("email")) {
                admin.setEmail((String) updateData.get("email"));
            }
            if (updateData.containsKey("avatar")) {
                admin.setAvatar((String) updateData.get("avatar"));
            }

            // 更新到数据库
            boolean success = adminService.updateById(admin);

            if (success) {
                // 重新查询以获取完整信息（包括角色信息）
                Admin updatedAdmin = adminService.getById(adminId);
                if (updatedAdmin != null && updatedAdmin.getUsername() != null) {
                    updatedAdmin = adminService.getByUsernameWithRole(updatedAdmin.getUsername());
                }

                Map<String, Object> adminInfo = new HashMap<>();
                if (updatedAdmin != null) {
                    adminInfo.put("adminId", updatedAdmin.getAdminId());
                    adminInfo.put("username", updatedAdmin.getUsername() != null ? updatedAdmin.getUsername() : "");
                    adminInfo.put("realName", updatedAdmin.getRealName() != null ? updatedAdmin.getRealName() : "");
                    adminInfo.put("phone", updatedAdmin.getPhone() != null ? updatedAdmin.getPhone() : "");
                    adminInfo.put("email", updatedAdmin.getEmail() != null ? updatedAdmin.getEmail() : "");
                    adminInfo.put("avatar", updatedAdmin.getAvatar() != null ? updatedAdmin.getAvatar() : "");
                    adminInfo.put("roleCode", updatedAdmin.getRoleCode() != null ? updatedAdmin.getRoleCode() : "");
                    adminInfo.put("roleName", updatedAdmin.getRoleName() != null ? updatedAdmin.getRoleName() : "");
                    adminInfo.put("status", updatedAdmin.getStatus() != null ? updatedAdmin.getStatus() : "");
                    adminInfo.put("createTime", updatedAdmin.getCreateTime() != null ? updatedAdmin.getCreateTime().toString() : "");
                }

                response.put("success", true);
                response.put("message", "更新成功");
                response.put("admin", adminInfo);
                return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
            } else {
                response.put("success", false);
                response.put("message", "更新失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "系统错误：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 上传管理员头像 - Base64格式
     */
    @ApiOperation("上传管理员头像")
    @PutMapping(value = "/profile/avatar/base64", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, Object>> uploadAvatarBase64(@RequestBody Map<String, Object> base64Data) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 从JWT中获取当前管理员信息
            Long adminId = AdminContext.getAdminId();

            if (adminId == null) {
                response.put("success", false);
                response.put("message", "管理员信息不存在");
                return ResponseEntity.status(401).body(response);
            }

            // 获取base64字符串
            String base64Str = (String) base64Data.get("avatarBase64");
            if (base64Str == null || base64Str.isEmpty()) {
                response.put("success", false);
                response.put("message", "base64头像不能为空");
                return ResponseEntity.status(400).body(response);
            }

            // 获取管理员信息
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                response.put("success", false);
                response.put("message", "管理员不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 上传Base64图片（按管理员ID分类存储）
            String fileName = FileUploadUtil.uploadBase64Image(base64Str, fileUploadConfig.getUploadPath(), "admin_" + adminId);
            // 生成图片URL
            String avatarUrl = fileUploadConfig.getUrlPrefix() + fileName;
            // 更新管理员头像
            admin.setAvatar(avatarUrl);

            // 更新到数据库
            boolean success = adminService.updateById(admin);

            if (success) {
                // 将头像转换为base64编码用于前端显示
                String avatarBase64 = convertAvatarToBase64(avatarUrl);

                response.put("success", true);
                response.put("message", "头像上传成功");
                response.put("avatar", avatarBase64);
                return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(response);
            } else {
                response.put("success", false);
                response.put("message", "头像上传失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("管理员头像上传参数错误: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (IOException e) {
            log.error("管理员头像上传IO错误: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "图片上传失败");
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            log.error("管理员头像上传系统错误", e);
            response.put("success", false);
            response.put("message", "系统错误：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 将管理员头像转换为base64编码
     * @param avatarUrl 管理员头像的URL路径
     * @return base64编码的头像字符串，或null如果转换失败
     */
    private String convertAvatarToBase64(String avatarUrl) {
        if (avatarUrl == null) {
            return null;
        }

        try {
            // 拼接完整的图片路径
            String fullPath = fileUploadConfig.getUploadPath() + avatarUrl.substring(fileUploadConfig.getUrlPrefix().length());
            File avatarFile = new File(fullPath);

            if (avatarFile.exists()) {
                byte[] imageBytes = Files.readAllBytes(avatarFile.toPath());
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            }
        } catch (IOException e) {
            log.error("Failed to convert admin avatar to base64: {}", e.getMessage());
        }

        return null;
    }
}
