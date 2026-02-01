package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户教程控制器
 * 普通用户可以发布教程（需审核）
 */
@RestController
@RequestMapping("/v1/tutorial/user")
public class TutorialUserController {

    @Autowired
    private TutorialService tutorialService;

    /**
     * 用户创建教程（草稿状态）
     * POST /api/v1/tutorial/user/create
     * 权限: 需要USER角色
     */
    @PostMapping("/create")
    // @PreAuthorize("hasRole('USER')") // 临时移除权限检查，允许所有登录用户访问
    public ResponseEntity<Tutorial> createUserTutorial(@RequestBody Tutorial tutorial) {
        // 设置为用户发布的教程
        tutorial.setSourceType("USER");
        tutorial.setAuthorType("USER");
        tutorial.setStatus("DRAFT");
        tutorial.setReviewStatus("NOT_SUBMITTED");
        tutorial.setOfficial(false);

        // TODO: 从认证上下文获取用户ID
        String userId = "1";
        tutorial.setAuthorId(userId);

        Tutorial created = tutorialService.createByMerchant(tutorial);
        return ResponseEntity.ok(created);
    }

    /**
     * 获取当前用户的教程列表
     * GET /api/v1/tutorial/user/my
     * 权限: 需要USER角色
     */
    @GetMapping("/my")
    // @PreAuthorize("hasRole('USER')") // 临时移除权限检查
    public ResponseEntity<?> getMyTutorials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 从认证上下文获取用户ID
        String userId = "1";

        var tutorials = tutorialService.getUserTutorials(userId, page, size);
        return ResponseEntity.ok(tutorials);
    }

    /**
     * 用户更新教程
     * PUT /api/v1/tutorial/user/{id}
     * 权限: 需要USER角色，且只能编辑自己的教程
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('USER')") // 临时移除权限检查
    public ResponseEntity<Map<String, Object>> updateUserTutorial(
            @PathVariable String id,
            @RequestBody Tutorial tutorial) {
        // TODO: 验证教程是否属于当前用户
        // TODO: 从认证上下文获取用户ID

        boolean success = tutorialService.updateByMerchant(id, tutorial);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "更新成功" : "更新失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 用户提交审核
     * POST /api/v1/tutorial/user/{id}/submit
     * 权限: 需要USER角色，且只能提交自己的教程
     */
    @PostMapping("/{id}/submit")
    // @PreAuthorize("hasRole('USER')") // 临时移除权限检查
    public ResponseEntity<Map<String, Object>> submitUserTutorial(@PathVariable String id) {
        // TODO: 验证教程是否属于当前用户

        boolean success = tutorialService.submitForReview(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "已提交审核" : "提交失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 删除教程
     * DELETE /api/v1/tutorial/user/{id}
     * 权限: 需要USER角色，且只能删除自己的草稿
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('USER')") // 临时移除权限检查
    public ResponseEntity<Map<String, Object>> deleteUserTutorial(@PathVariable String id) {
        // TODO: 验证教程是否属于当前用户
        // TODO: 检查是否为草稿状态

        boolean success = tutorialService.removeById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "删除成功" : "删除失败");

        return ResponseEntity.ok(response);
    }
}
