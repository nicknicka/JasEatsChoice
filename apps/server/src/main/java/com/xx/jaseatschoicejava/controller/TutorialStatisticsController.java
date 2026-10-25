package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.service.TutorialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 教程统计控制器
 */
@RestController
@RequestMapping("/v1/tutorial/stats")
public class TutorialStatisticsController {

    @Autowired
    private TutorialService tutorialService;

    /**
     * 增加浏览次数
     * POST /api/v1/tutorial/stats/{id}/view
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> incrementViewCount(@PathVariable String id) {
        boolean success = tutorialService.incrementViewCount(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);

        return ResponseEntity.ok(response);
    }

    /**
     * 用户评分
     * POST /api/v1/tutorial/stats/{id}/rating
     * Body: { "rating": 4.5 }
     */
    @PostMapping("/{id}/rating")
    public ResponseEntity<Map<String, Object>> rateTutorial(
            @PathVariable String id,
            @RequestBody Map<String, Double> request) {
        Double rating = request.get("rating");
        if (rating == null || rating < 0 || rating > 5) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "评分必须在0-5之间");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = tutorialService.updateRating(id, rating);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "评分成功" : "评分失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 收藏教程
     * POST /api/v1/tutorial/stats/{id}/favorite
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Object>> favoriteTutorial(@PathVariable String id) {
        boolean success = tutorialService.incrementFavoriteCount(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "收藏成功" : "收藏失败");

        return ResponseEntity.ok(response);
    }

    /**
     * 获取统计数据（管理员/商家）
     * GET /api/v1/tutorial/stats/overview
     * 权限: 需要ADMIN或MERCHANT角色
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public ResponseEntity<Map<String, Object>> getOverview() {
        // TODO: 实现统计数据查询
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTutorials", 0);
        stats.put("publishedTutorials", 0);
        stats.put("pendingReview", 0);
        stats.put("totalViews", 0);
        stats.put("totalFavorites", 0);
        stats.put("averageRating", 0.0);

        return ResponseEntity.ok(stats);
    }
}
