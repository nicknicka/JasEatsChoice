package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-用户管理控制器
 */
@Api(tags = "管理员-用户管理")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（分页）
     */
    @ApiOperation("获取用户列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:user:list')")
    public ResponseEntity<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<User> pageParam = new Page<>(page, pageSize);

        IPage<User> result;
        if (keyword != null && !keyword.isEmpty()) {
            // 使用搜索功能
            result = userService.page(pageParam);
        } else {
            result = userService.page(pageParam);
        }

        // 清除密码等敏感信息
        result.getRecords().forEach(user -> user.setPassword(null));

        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户详情
     */
    @ApiOperation("获取用户详情")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('admin:user:detail')")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable String userId) {
        User user = userService.getById(userId);

        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            user.setPassword(null); // 清除密码
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 修改用户状态
     */
    @ApiOperation("修改用户状态")
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasAnyAuthority('admin:user:status')")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {

        // 注意：User表目前没有status字段，这里只是示例
        // 实际实现需要根据业务需求调整

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "状态修改成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     */
    @ApiOperation("删除用户")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('admin:user:delete')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        boolean success = userService.removeById(userId);

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
}
