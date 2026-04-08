package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.*;
import java.util.List;

/**
 * 商家经营洞察服务接口
 */
public interface MerchantInsightService {

    /**
     * 获取核心指标
     * @param merchantId 商家ID
     * @param timeRange 时间范围：today/week/month
     * @return 核心指标数据
     */
    InsightMetricsDTO getMetrics(String merchantId, String timeRange);

    /**
     * 获取销售趋势
     * @param merchantId 商家ID
     * @param timeRange 时间范围
     * @return 销售趋势数据
     */
    List<SalesTrendItemDTO> getSalesTrend(String merchantId, String timeRange);

    /**
     * 获取热销菜品TOP 5
     * @param merchantId 商家ID
     * @param timeRange 时间范围
     * @return 热销菜品列表
     */
    List<TopDishDTO> getTopDishes(String merchantId, String timeRange);

    /**
     * 获取评价分布
     * @param merchantId 商家ID
     * @return 评价分布数据
     */
    List<RatingDistributionDTO> getRatingDistribution(String merchantId);

    /**
     * 生成AI经营建议
     * @param merchantId 商家ID
     * @param timeRange 时间范围
     * @return AI建议列表
     */
    List<AiSuggestionDTO> generateAiSuggestions(String merchantId, String timeRange);
}
