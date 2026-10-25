package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家列表控制器（复数路径，用于前端API对接）
 */
@RestController
@RequestMapping("/v1/merchants")
public class MerchantsController {

    private static final Logger logger = LoggerFactory.getLogger(MerchantsController.class);

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取商家列表
     */
    @GetMapping
    public ResponseResult<?> getMerchants(@RequestParam(required = false) String category,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        try {
            LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq(Merchant::getCategory, category);
            }

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                    .like(Merchant::getName, keyword)
                    .or()
                    .like(Merchant::getCategory, keyword)
                );
            }

            // 只显示营业的商家
            queryWrapper.eq(Merchant::getStatus, true);

            // 排序
            if ("rating".equals(sort)) {
                queryWrapper.orderByDesc(Merchant::getRating);
            } else {
                queryWrapper.orderByDesc(Merchant::getCreateTime);
            }

            // 分页
            queryWrapper.last("LIMIT " + ((page - 1) * size) + ", " + size);

            List<Merchant> merchants = merchantService.list(queryWrapper);

            // 隐藏敏感信息并转换为前端需要的格式
            List<Map<String, Object>> result = merchants.stream()
                .map(merchant -> convertMerchantToMap(merchant))
                .collect(Collectors.toList());

            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("获取商家列表失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "获取商家列表失败");
        }
    }

    /**
     * 获取附近商家
     */
    @GetMapping("/nearby")
    public ResponseResult<?> getNearbyMerchants(@RequestParam(required = false) Double latitude,
                                                @RequestParam(required = false) Double longitude,
                                                @RequestParam(defaultValue = "5000") Double radius,
                                                @RequestParam(defaultValue = "10") Integer limit) {
        try {
            LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();

            // 只显示营业的商家
            queryWrapper.eq(Merchant::getStatus, true);

            // 按评分排序
            queryWrapper.orderByDesc(Merchant::getRating);
            queryWrapper.last("LIMIT " + limit);

            List<Merchant> merchants = merchantService.list(queryWrapper);

            // 转换为前端需要的格式
            List<Map<String, Object>> result = merchants.stream()
                .map(merchant -> {
                    Map<String, Object> map = convertMerchantToMap(merchant);
                    // 如果有经纬度，可以计算距离并添加到map中
                    if (latitude != null && longitude != null &&
                        merchant.getLatitude() != null && merchant.getLongitude() != null) {
                        Double distance = calculateDistance(
                            latitude, longitude,
                            merchant.getLatitude().doubleValue(),
                            merchant.getLongitude().doubleValue()
                        );
                        map.put("distance", distance);
                    }
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("获取附近商家失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "获取附近商家失败");
        }
    }

    /**
     * 获取商家详情
     */
    @GetMapping("/{merchantId}")
    public ResponseResult<?> getMerchantDetail(@PathVariable String merchantId) {
        try {
            Merchant merchant = merchantService.getById(merchantId);
            if (merchant != null) {
                Map<String, Object> result = convertMerchantToMap(merchant);
                return ResponseResult.success(result);
            }
            return ResponseResult.fail("404", "商家不存在");
        } catch (Exception e) {
            logger.error("获取商家详情失败: {}", e.getMessage(), e);
            return ResponseResult.fail("500", "获取商家详情失败");
        }
    }

    /**
     * 将Merchant实体转换为Map
     */
    private Map<String, Object> convertMerchantToMap(Merchant merchant) {
        Map<String, Object> map = new HashMap<>();
        map.put("merchantId", merchant.getId());
        map.put("id", merchant.getId());
        map.put("merchantName", merchant.getName());
        map.put("name", merchant.getName());
        map.put("avatar", merchant.getAvatar());
        map.put("logo", merchant.getAvatar());
        map.put("category", merchant.getCategory());
        map.put("address", merchant.getAddress());
        map.put("latitude", merchant.getLatitude());
        map.put("longitude", merchant.getLongitude());
        map.put("rating", merchant.getRating() != null ? merchant.getRating() : BigDecimal.ZERO);
        map.put("score", merchant.getRating() != null ? merchant.getRating() : BigDecimal.ZERO);
        map.put("monthlySales", 0); // 默认值
        map.put("sales", 0); // 默认值
        map.put("averagePrice", merchant.getAveragePrice());
        map.put("businessHours", merchant.getBusinessHours());
        map.put("businessScope", merchant.getBusinessScope());
        map.put("album", merchant.getAlbum());
        map.put("tags", new String[]{}); // 默认空数组
        return map;
    }

    /**
     * 计算两点之间的距离（单位：米）
     * 使用Haversine公式
     */
    private Double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径，单位：米

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
