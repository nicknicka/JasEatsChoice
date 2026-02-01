package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 教程管理控制器 - 管理员接口
 */
@RestController
@RequestMapping("/v1/tutorial/admin")
public class TutorialAdminController {

    @Autowired
    private TutorialService tutorialService;

    /**
     * 获取所有教程（管理员专用）
     * GET /api/v1/tutorial/admin/list
     * 权限: 需要ADMIN角色
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tutorial>> getAllTutorials() {
        List<Tutorial> tutorials = tutorialService.getAllTutorialsForAdmin();
        return ResponseEntity.ok(tutorials);
    }

    /**
     * 管理员创建教程（直接发布）
     * POST /api/v1/tutorial/admin/create
     * 权限: 需要ADMIN角色
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tutorial> createByAdmin(@RequestBody Tutorial tutorial) {
        Tutorial created = tutorialService.createByAdmin(tutorial);
        return ResponseEntity.ok(created);
    }

    /**
     * 获取待审核的教程列表
     * GET /api/v1/tutorial/admin/pending?page=0&size=10
     * 权限: 需要ADMIN角色
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Tutorial>> getPendingTutorials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Tutorial> tutorials = tutorialService.getPendingTutorials(page, size);
        return ResponseEntity.ok(tutorials);
    }

    /**
     * 审核通过
     * POST /api/v1/tutorial/admin/{id}/approve
     * Body: { "comment": "审核意见", "setFeatured": true }
     * 权限: 需要ADMIN角色
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveTutorial(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        // TODO: 从Spring Security上下文获取管理员ID
        String reviewerId = "1";
        String comment = (String) request.get("comment");
        Boolean setFeatured = (Boolean) request.getOrDefault("setFeatured", false);

        boolean success = tutorialService.approveTutorial(id, reviewerId, comment, setFeatured);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "审核通过" : "审核失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 审核拒绝
     * POST /api/v1/tutorial/admin/{id}/reject
     * Body: { "comment": "拒绝原因" }
     * 权限: 需要ADMIN角色
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectTutorial(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        // TODO: 从Spring Security上下文获取管理员ID
        String reviewerId = "1";
        String comment = request.get("comment");

        boolean success = tutorialService.rejectTutorial(id, reviewerId, comment);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "已拒绝" : "操作失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 设置/取消精选
     * PUT /api/v1/tutorial/admin/{id}/featured
     * Body: { "featured": true }
     * 权限: 需要ADMIN角色
     */
    @PutMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleFeatured(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {
        Boolean featured = request.get("featured");
        boolean success = tutorialService.toggleFeatured(id, featured);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? (featured ? "已设为精选" : "已取消精选") : "操作失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 删除教程
     * DELETE /api/v1/tutorial/admin/{id}
     * 权限: 需要ADMIN角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTutorial(@PathVariable String id) {
        boolean success = tutorialService.removeById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "删除成功" : "删除失败");

        return ResponseEntity.ok(response);
    }
}
