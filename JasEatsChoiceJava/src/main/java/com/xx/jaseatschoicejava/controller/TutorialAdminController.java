package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
     * 管理员创建教程（直接发布）
     * POST /api/v1/tutorial/admin/create
     */
    @PostMapping("/create")
    public ResponseEntity<Tutorial> createByAdmin(@RequestBody Tutorial tutorial) {
        Tutorial created = tutorialService.createByAdmin(tutorial);
        return ResponseEntity.ok(created);
    }

    /**
     * 获取待审核的教程列表
     * GET /api/v1/tutorial/admin/pending?page=0&size=10
     */
    @GetMapping("/pending")
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
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveTutorial(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        String reviewerId = "1"; // TODO: 从认证上下文获取管理员ID
        String comment = (String) request.get("comment");
        Boolean setFeatured = (Boolean) request.getOrDefault("setFeatured", false);

        boolean success = tutorialService.approveTutorial(id, Long.valueOf(reviewerId), comment, setFeatured);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "审核通过" : "审核失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 审核拒绝
     * POST /api/v1/tutorial/admin/{id}/reject
     * Body: { "comment": "拒绝原因" }
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectTutorial(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String reviewerId = "1"; // TODO: 从认证上下文获取管理员ID
        String comment = request.get("comment");

        boolean success = tutorialService.rejectTutorial(id, Long.valueOf(reviewerId), comment);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "已拒绝" : "操作失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 设置/取消精选
     * PUT /api/v1/tutorial/admin/{id}/featured
     * Body: { "featured": true }
     */
    @PutMapping("/{id}/featured")
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
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTutorial(@PathVariable String id) {
        boolean success = tutorialService.removeById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "删除成功" : "删除失败");

        return ResponseEntity.ok(response);
    }
}
