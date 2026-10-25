package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.service.MerchantInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家经营洞察控制器
 * 提供AI经营分析功能
 */
@RestController
@RequestMapping("/v1/merchant/insight")
public class MerchantInsightController {

    @Autowired
    private MerchantInsightService merchantInsightService;

    /**
     * 获取核心指标
     */
    @GetMapping("/{merchantId}/metrics")
    public ResponseResult<?> getMetrics(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            InsightMetricsDTO metrics = merchantInsightService.getMetrics(merchantId, timeRange);
            return ResponseResult.success(metrics);
        } catch (Exception e) {
            return ResponseResult.fail("500", "获取指标失败：" + e.getMessage());
        }
    }

    /**
     * 获取销售趋势
     */
    @GetMapping("/{merchantId}/trend")
    public ResponseResult<?> getSalesTrend(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            List<SalesTrendItemDTO> trend = merchantInsightService.getSalesTrend(merchantId, timeRange);
            return ResponseResult.success(trend);
        } catch (Exception e) {
            return ResponseResult.fail("500", "获取趋势失败：" + e.getMessage());
        }
    }

    /**
     * 获取热销菜品TOP 5
     */
    @GetMapping("/{merchantId}/top-dishes")
    public ResponseResult<?> getTopDishes(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            List<TopDishDTO> topDishes = merchantInsightService.getTopDishes(merchantId, timeRange);
            return ResponseResult.success(topDishes);
        } catch (Exception e) {
            return ResponseResult.fail("500", "获取热销菜品失败：" + e.getMessage());
        }
    }

    /**
     * 获取评价分布
     */
    @GetMapping("/{merchantId}/rating-distribution")
    public ResponseResult<?> getRatingDistribution(@PathVariable String merchantId) {
        try {
            List<RatingDistributionDTO> distribution = merchantInsightService.getRatingDistribution(merchantId);
            return ResponseResult.success(distribution);
        } catch (Exception e) {
            return ResponseResult.fail("500", "获取评价分布失败：" + e.getMessage());
        }
    }

    /**
     * 生成AI经营建议
     */
    @PostMapping("/{merchantId}/ai-suggestions")
    public ResponseResult<?> generateAiSuggestions(
            @PathVariable String merchantId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String timeRange = request != null ? request.getOrDefault("timeRange", "week") : "week";
            List<AiSuggestionDTO> suggestions = merchantInsightService.generateAiSuggestions(merchantId, timeRange);
            return ResponseResult.success(suggestions);
        } catch (Exception e) {
            return ResponseResult.fail("500", "生成AI建议失败：" + e.getMessage());
        }
    }

    /**
     * 获取完整经营洞察（一次性获取所有数据）
     */
    @GetMapping("/{merchantId}/full")
    public ResponseResult<?> getFullInsight(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "week") String timeRange) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("metrics", merchantInsightService.getMetrics(merchantId, timeRange));
            result.put("salesTrend", merchantInsightService.getSalesTrend(merchantId, timeRange));
            result.put("topDishes", merchantInsightService.getTopDishes(merchantId, timeRange));
            result.put("ratingDistribution", merchantInsightService.getRatingDistribution(merchantId));
            result.put("aiSuggestions", merchantInsightService.generateAiSuggestions(merchantId, timeRange));
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail("500", "获取经营洞察失败：" + e.getMessage());
        }
    }
}
