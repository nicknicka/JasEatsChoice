package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.RejectRecommendation;
import com.xx.jaseatschoicejava.service.RejectRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐拒绝Controller
 */
@RestController
@RequestMapping("/v1/recommendations/rejects")
public class RejectRecommendationController {

    private final RejectRecommendationService rejectRecommendationService;

    @Autowired
    public RejectRecommendationController(RejectRecommendationService rejectRecommendationService) {
        this.rejectRecommendationService = rejectRecommendationService;
    }

    /**
     * 记录用户拒绝推荐
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @param reason 拒绝原因（可选）
     * @return 拒绝记录
     */
    @PostMapping
    public ResponseResult<?> addRejectRecord(
            @RequestParam String userId,
            @RequestParam String dishId,
            @RequestParam(required = false) String reason) {
        RejectRecommendation record = rejectRecommendationService.addRejectRecord(userId, dishId, reason);
        return ResponseResult.success(record);
    }

    /**
     * 统计用户对某个菜品的拒绝次数
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @return 拒绝次数
     */
    @GetMapping("/count")
    public ResponseResult<?> countRejects(
            @RequestParam String userId,
            @RequestParam String dishId) {
        int count = rejectRecommendationService.countRejects(userId, dishId);
        return ResponseResult.success(count);
    }

    /**
     * 查询用户所有被拒绝的菜品ID列表
     * @param userId 用户ID
     * @return 菜品ID列表
     */
    @GetMapping("/list")
    public ResponseResult<?> getRejectedDishIds(@RequestParam String userId) {
        List<String> dishIds = rejectRecommendationService.getRejectedDishIds(userId);
        return ResponseResult.success(dishIds);
    }

    /**
     * 查询用户被拒绝超过指定次数的菜品ID列表
     * @param userId 用户ID
     * @param threshold 拒绝次数阈值（默认2）
     * @return 菜品ID列表
     */
    @GetMapping("/frequent")
    public ResponseResult<?> getFrequentlyRejectedDishIds(
            @RequestParam String userId,
            @RequestParam(defaultValue = "2") int threshold) {
        List<String> dishIds = rejectRecommendationService.getFrequentlyRejectedDishIds(userId, threshold);
        return ResponseResult.success(dishIds);
    }

    /**
     * 清除用户对某个菜品的拒绝记录
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @return 是否成功
     */
    @DeleteMapping
    public ResponseResult<?> clearRejectRecord(
            @RequestParam String userId,
            @RequestParam String dishId) {
        boolean success = rejectRecommendationService.clearRejectRecord(userId, dishId);
        return success ? ResponseResult.success() : ResponseResult.fail("500", "清除失败");
    }
}
