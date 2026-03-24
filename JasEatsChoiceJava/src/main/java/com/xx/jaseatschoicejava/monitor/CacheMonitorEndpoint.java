package com.xx.jaseatschoicejava.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存监控Actuator端点
 *
 * 访问路径：
 * - GET /actuator/cachestats - 获取全局统计
 * - GET /actuator/cachestats/{cacheName} - 获取指定缓存统计
 * - GET /actuator/cachestats/report - 获取完整报告
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Component
@Endpoint(id = "cachestats")
public class CacheMonitorEndpoint {

    @Autowired
    private CacheMonitor cacheMonitor;

    /**
     * 获取全局缓存统计
     *
     * @return 全局统计信息
     */
    @ReadOperation
    public Map<String, Object> globalStats() {
        CacheMonitor.CacheStats stats = cacheMonitor.getGlobalStats();
        return buildStatsMap("global", stats);
    }

    /**
     * 获取指定缓存的统计
     *
     * @param cacheName 缓存名称
     * @return 缓存统计信息
     */
    @ReadOperation
    public Map<String, Object> cacheStats(@Selector String cacheName) {
        CacheMonitor.CacheStats stats = cacheMonitor.getCacheStats(cacheName);
        return buildStatsMap(cacheName, stats);
    }

    /**
     * 获取所有缓存的统计
     *
     * @return 所有缓存统计
     */
    @ReadOperation
    public Map<String, Object> allStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("global", buildStatsMap("global", cacheMonitor.getGlobalStats()));

        Map<String, Map<String, Object>> caches = new HashMap<>();
        // 添加所有已知缓存
        String[] knownCaches = {
            "user:preference",
            "dish:detail",
            "address:list",
            "user:info",
            "user:info:phone",
            "merchant:detail",
            "order:detail"
        };

        for (String cacheName : knownCaches) {
            CacheMonitor.CacheStats stats = cacheMonitor.getCacheStats(cacheName);
            if (stats.getTotalCount() > 0) {
                caches.put(cacheName, buildStatsMap(cacheName, stats));
            }
        }

        result.put("caches", caches);
        return result;
    }

    /**
     * 获取监控报告
     *
     * @return 监控报告
     */
    @ReadOperation
    public String report() {
        return cacheMonitor.getReport();
    }

    /**
     * 重置所有统计
     *
     * @return 操作结果
     */
    @org.springframework.boot.actuate.endpoint.annotation.WriteOperation
    public Map<String, String> reset() {
        cacheMonitor.reset();
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "All cache statistics have been reset");
        log.info("缓存统计已通过Actuator端点重置");
        return result;
    }

    /**
     * 重置指定缓存的统计
     *
     * @param cacheName 缓存名称
     * @return 操作结果
     */
    @org.springframework.boot.actuate.endpoint.annotation.WriteOperation
    public Map<String, String> reset(@Selector String cacheName) {
        cacheMonitor.reset(cacheName);
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Cache statistics have been reset: " + cacheName);
        log.info("缓存统计已通过Actuator端点重置: cacheName={}", cacheName);
        return result;
    }

    /**
     * 构建统计信息Map
     *
     * @param name 名称
     * @param stats 统计对象
     * @return 统计信息Map
     */
    private Map<String, Object> buildStatsMap(String name, CacheMonitor.CacheStats stats) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("hitCount", stats.getHitCount());
        map.put("missCount", stats.getMissCount());
        map.put("putCount", stats.getPutCount());
        map.put("evictCount", stats.getEvictCount());
        map.put("totalCount", stats.getTotalCount());
        map.put("hitRate", stats.getHitRate());
        map.put("missRate", stats.getMissRate());
        map.put("avgResponseTime", stats.getAverageResponseTime());
        return map;
    }
}
