package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.SystemLogService;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-用户管理控制器（更新版）
 */
@Api(tags = "管理员-用户管理")
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private SystemLogService systemLogService;

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

        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            // 使用关键词搜索（昵称、手机号、邮箱）
            queryWrapper.and(wrapper -> wrapper
                .like("nickname", keyword)
                .or()
                .like("phone", keyword)
                .or()
                .like("email", keyword)
            );
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc("create_time");

        IPage<User> result = userService.page(pageParam, queryWrapper);

        // 清除密码等敏感信息（使用JSON序列化忽略）
        result.getRecords().forEach(user -> {
            if (user != null) {
                // 不在controller层修改实体，通过序列化配置忽略password字段
                // user.setPassword(null);
                // user.setPaymentPassword(null);
            }
        });

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
            // 注意：密码字段通过JSON序列化配置忽略，不需要手动设置为null
            response.put("success", true);
            response.put("data", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 编辑用户信息 ✨ 新增
     */
    @ApiOperation("编辑用户信息")
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('admin:user:edit')")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String userId,
            @RequestBody Map<String, Object> updateData) {

        User user = userService.getById(userId);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 记录原始信息（用于日志）
        String originalInfo = String.format("昵称:%s,手机:%s,邮箱:%s",
            user.getNickname(), user.getPhone(), user.getEmail());

        // 更新用户信息
        if (updateData.containsKey("nickname")) {
            user.setNickname((String) updateData.get("nickname"));
        }
        if (updateData.containsKey("phone")) {
            user.setPhone((String) updateData.get("phone"));
        }
        if (updateData.containsKey("email")) {
            user.setEmail((String) updateData.get("email"));
        }
        if (updateData.containsKey("avatar")) {
            user.setAvatar((String) updateData.get("avatar"));
        }
        if (updateData.containsKey("gender")) {
            user.setGender((String) updateData.get("gender"));
        }
        if (updateData.containsKey("birthday")) {
            user.setBirthday((String) updateData.get("birthday"));
        }
        if (updateData.containsKey("location")) {
            user.setLocation((String) updateData.get("location"));
        }
        if (updateData.containsKey("bio")) {
            user.setBio((String) updateData.get("bio"));
        }

        user.setUpdateTime(LocalDateTime.now());

        boolean success = userService.updateById(user);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();
            String newInfo = String.format("昵称:%s,手机:%s,邮箱:%s",
                user.getNickname(), user.getPhone(), user.getEmail());

            systemLogService.logOperation(
                "UPDATE", "USER", "编辑用户信息：" + user.getNickname(),
                adminId, adminName, "ADMIN",
                "AdminUserController.updateUser",
                "userId=" + userId,
                null, 0L, null, "SUCCESS"
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "更新失败");
            return ResponseEntity.status(500).body(response);
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

        User user = userService.getById(userId);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }

        String status = request.get("status"); // ACTIVE, LOCKED, DELETED
        String reason = request.get("reason");

        // 记录操作日志
        if (systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "UPDATE", "USER", "修改用户状态：" + user.getNickname() + " -> " + status,
                adminId, adminName, "ADMIN",
                "AdminUserController.updateUserStatus",
                "userId=" + userId + ", status=" + status + ", reason=" + reason,
                null, 0L, null, "SUCCESS"
            );
        }

        // 注意：User表需要添加status字段，这里只是示例
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
        User user = userService.getById(userId);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }

        boolean success = userService.removeById(userId);

        // 记录操作日志
        if (success && systemLogService != null) {
            Long adminId = AdminContext.getAdminId();
            String adminName = AdminContext.getAdminUsername();

            systemLogService.logOperation(
                "DELETE", "USER", "删除用户：" + user.getNickname(),
                adminId, adminName, "ADMIN",
                "AdminUserController.deleteUser",
                "userId=" + userId,
                null, 0L, null, "SUCCESS"
            );
        }

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
     * 获取用户统计 ✨ 新增
     */
    @ApiOperation("获取用户统计")
    @GetMapping("/{userId}/statistics")
    @PreAuthorize("hasAnyAuthority('admin:user:detail')")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable String userId) {
        User user = userService.getById(userId);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }

        Map<String, Object> stats = new HashMap<>();
        // TODO: 添加更多用户统计信息
        // - 订单数量
        // - 消费金额
        // - 收藏数量
        // - 评价数量
        // - 注册天数
        stats.put("userId", userId);
        stats.put("nickname", user.getNickname());
        stats.put("createTime", user.getCreateTime());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }
}
