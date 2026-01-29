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
 * 教程商家控制器 - 商家接口
 */
@RestController
@RequestMapping("/v1/tutorial/merchant")
public class TutorialMerchantController {

    @Autowired
    private TutorialService tutorialService;

    /**
     * 商家创建教程（草稿状态）
     * POST /api/v1/tutorial/merchant/create
     */
    @PostMapping("/create")
    public ResponseEntity<Tutorial> createByMerchant(@RequestBody Tutorial tutorial) {
        // TODO: 从认证上下文获取商家ID
        Long merchantId = 1L;
        tutorial.setAuthorId(merchantId);
        tutorial.setLinkedMerchantId(merchantId);

        Tutorial created = tutorialService.createByMerchant(tutorial);
        return ResponseEntity.ok(created);
    }

    /**
     * 商家更新教程
     * PUT /api/v1/tutorial/merchant/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateByMerchant(
            @PathVariable String id,
            @RequestBody Tutorial tutorial) {
        // TODO: 验证商家权限
        boolean success = tutorialService.updateByMerchant(id, tutorial);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "更新成功" : "更新失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 商家提交审核
     * POST /api/v1/tutorial/merchant/{id}/submit
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Map<String, Object>> submitForReview(@PathVariable String id) {
        // TODO: 验证商家权限
        boolean success = tutorialService.submitForReview(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "已提交审核" : "提交失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取商家的教程列表
     * GET /api/v1/tutorial/merchant/my?page=0&size=10
     */
    @GetMapping("/my")
    public ResponseEntity<Page<Tutorial>> getMerchantTutorials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 从认证上下文获取商家ID
        Long merchantId = 1L;
        Page<Tutorial> tutorials = tutorialService.getMerchantTutorials(merchantId, page, size);
        return ResponseEntity.ok(tutorials);
    }

    /**
     * 删除自己的教程（仅草稿状态）
     * DELETE /api/v1/tutorial/merchant/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTutorial(@PathVariable String id) {
        // TODO: 验证商家权限和教程状态
        boolean success = tutorialService.removeById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "删除成功" : "删除失败");

        return ResponseEntity.ok(response);
    }
}
