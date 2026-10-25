package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.UserStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户统计控制器
 */
@Slf4j
@Api(tags = "用户统计分析")
@RestController
@RequestMapping("/v1/user-statistics")
@RequiredArgsConstructor
public class UserStatisticsController {

    private final UserStatisticsService userStatisticsService;

    /**
     * 获取用户消费统计
     * @param userId 用户ID
     * @param days 统计天数（默认30天）
     * @return 消费统计数据
     */
    @ApiOperation("获取用户消费统计")
    @GetMapping("/{userId}/consume")
    public ResponseResult<?> getConsumeStatistics(
            @PathVariable String userId,
            @RequestParam(defaultValue = "30") Integer days) {
        try {
            return ResponseResult.success(userStatisticsService.getConsumeStatistics(userId, days));
        } catch (Exception e) {
            log.error("获取用户消费统计失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取消费统计失败");
        }
    }

    /**
     * 获取用户卡路里摄入统计
     * @param userId 用户ID
     * @param days 统计天数（默认7天）
     * @return 卡路里摄入统计数据
     */
    @ApiOperation("获取用户卡路里摄入统计")
    @GetMapping("/{userId}/calories")
    public ResponseResult<?> getCaloriesStatistics(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            return ResponseResult.success(userStatisticsService.getCaloriesStatistics(userId, days));
        } catch (Exception e) {
            log.error("获取用户卡路里统计失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取卡路里统计失败");
        }
    }

    /**
     * 获取用户饮食记录统计
     * @param userId 用户ID
     * @param days 统计天数（默认30天）
     * @return 饮食记录统计数据
     */
    @ApiOperation("获取用户饮食记录统计")
    @GetMapping("/{userId}/diet-records")
    public ResponseResult<?> getDietRecordsStatistics(
            @PathVariable String userId,
            @RequestParam(defaultValue = "30") Integer days) {
        try {
            return ResponseResult.success(userStatisticsService.getDietRecordsStatistics(userId, days));
        } catch (Exception e) {
            log.error("获取用户饮食记录统计失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取饮食记录统计失败");
        }
    }

    /**
     * 获取用户收藏菜品统计
     * @param userId 用户ID
     * @return 收藏菜品统计数据
     */
    @ApiOperation("获取用户收藏菜品统计")
    @GetMapping("/{userId}/favorites")
    public ResponseResult<?> getFavoritesStatistics(@PathVariable String userId) {
        try {
            return ResponseResult.success(userStatisticsService.getFavoritesStatistics(userId));
        } catch (Exception e) {
            log.error("获取用户收藏统计失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取收藏统计失败");
        }
    }

    /**
     * 获取用户综合统计概览
     * @param userId 用户ID
     * @return 综合统计数据
     */
    @ApiOperation("获取用户综合统计概览")
    @GetMapping("/{userId}/overview")
    public ResponseResult<?> getOverviewStatistics(@PathVariable String userId) {
        try {
            return ResponseResult.success(userStatisticsService.getOverviewStatistics(userId));
        } catch (Exception e) {
            log.error("获取用户综合统计失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取综合统计失败");
        }
    }
}
