package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.monitor.CacheMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存监控Dashboard Controller
 *
 * 提供简单的Web API访问缓存监控数据
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@RestController
@RequestMapping("/admin/cache")
public class CacheMonitorController {

    @Autowired
    private CacheMonitor cacheMonitor;

    /**
     * 获取缓存监控概览
     *
     * @return 监控数据
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        log.debug("获取缓存监控概览");

        Map<String, Object> result = new HashMap<>();
        CacheMonitor.CacheStats globalStats = cacheMonitor.getGlobalStats();

        result.put("global", buildStatsMap(globalStats));
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 获取详细统计
     *
     * @return 详细统计
     */
    @GetMapping("/details")
    public Map<String, Object> getDetails() {
        log.debug("获取缓存详细统计");

        Map<String, Object> result = new HashMap<>();

        // 全局统计
        result.put("global", buildStatsMap(cacheMonitor.getGlobalStats()));

        // 各缓存统计
        Map<String, Map<String, Object>> caches = new HashMap<>();
        String[] cacheNames = {
            "user:preference",
            "dish:detail",
            "address:list",
            "user:info",
            "user:info:phone",
            "merchant:detail",
            "order:detail"
        };

        for (String cacheName : cacheNames) {
            CacheMonitor.CacheStats stats = cacheMonitor.getCacheStats(cacheName);
            caches.put(cacheName, buildStatsMap(stats));
        }

        result.put("caches", caches);
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 获取文本报告
     *
     * @return 文本报告
     */
    @GetMapping("/report")
    public Map<String, String> getReport() {
        log.debug("获取缓存监控报告");

        Map<String, String> result = new HashMap<>();
        result.put("report", cacheMonitor.getReport());
        result.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return result;
    }

    /**
     * 重置统计
     *
     * @return 操作结果
     */
    @GetMapping("/reset")
    public Map<String, String> resetStats() {
        log.info("重置缓存监控统计");

        cacheMonitor.reset();

        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "缓存监控统计已重置");
        result.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return result;
    }

    /**
     * 获取健康状态
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        log.debug("获取缓存健康状态");

        Map<String, Object> result = new HashMap<>();
        CacheMonitor.CacheStats stats = cacheMonitor.getGlobalStats();

        // 判断健康状态
        String status = "healthy";
        if (stats.getTotalCount() > 0) {
            double hitRate = stats.getHitRate();
            if (hitRate < 0.5) {
                status = "warning";  // 命中率低于50%
            } else if (hitRate < 0.3) {
                status = "critical"; // 命中率低于30%
            }
        }

        result.put("status", status);
        result.put("hitRate", stats.getHitRate());
        result.put("totalCount", stats.getTotalCount());
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 构建统计Map
     *
     * @param stats 统计对象
     * @return 统计Map
     */
    private Map<String, Object> buildStatsMap(CacheMonitor.CacheStats stats) {
        Map<String, Object> map = new HashMap<>();
        map.put("hitCount", stats.getHitCount());
        map.put("missCount", stats.getMissCount());
        map.put("putCount", stats.getPutCount());
        map.put("evictCount", stats.getEvictCount());
        map.put("totalCount", stats.getTotalCount());
        map.put("hitRate", stats.getHitRate());
        map.put("hitRatePercent", String.format("%.2f%%", stats.getHitRate() * 100));
        map.put("missRate", stats.getMissRate());
        map.put("avgResponseTime", stats.getAverageResponseTime());
        return map;
    }
}
