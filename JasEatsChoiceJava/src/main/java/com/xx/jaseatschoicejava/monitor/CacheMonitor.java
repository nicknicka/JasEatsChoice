package com.xx.jaseatschoicejava.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 缓存监控组件
 *
 * 功能：
 * 1. 统计缓存命中率
 * 2. 统计缓存响应时间
 * 3. 统计各缓存区域的性能
 * 4. 提供监控指标查询
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Component
public class CacheMonitor {

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final LongAdder hitCount = new LongAdder();
        private final LongAdder missCount = new LongAdder();
        private final LongAdder putCount = new LongAdder();
        private final LongAdder evictCount = new LongAdder();
        private final AtomicLong totalResponseTime = new AtomicLong(0);

        public void recordHit() {
            hitCount.increment();
        }

        public void recordMiss() {
            missCount.increment();
        }

        public void recordPut() {
            putCount.increment();
        }

        public void recordEvict() {
            evictCount.increment();
        }

        public void recordResponse(long milliseconds) {
            totalResponseTime.addAndGet(milliseconds);
        }

        public long getHitCount() {
            return hitCount.sum();
        }

        public long getMissCount() {
            return missCount.sum();
        }

        public long getPutCount() {
            return putCount.sum();
        }

        public long getEvictCount() {
            return evictCount.sum();
        }

        public long getTotalCount() {
            return getHitCount() + getMissCount();
        }

        public double getHitRate() {
            long total = getTotalCount();
            return total == 0 ? 0.0 : (double) getHitCount() / total;
        }

        public double getMissRate() {
            long total = getTotalCount();
            return total == 0 ? 0.0 : (double) getMissCount() / total;
        }

        public double getAverageResponseTime() {
            long total = getTotalCount();
            return total == 0 ? 0.0 : (double) totalResponseTime.get() / total;
        }

        @Override
        public String toString() {
            return String.format(
                "CacheStats{hits=%d, misses=%d, puts=%d, evicts=%d, hitRate=%.2f%%, avgResponse=%.2fms}",
                getHitCount(), getMissCount(), getPutCount(), getEvictCount(),
                getHitRate() * 100, getAverageResponseTime()
            );
        }
    }

    /**
     * 全局缓存统计
     */
    private final CacheStats globalStats = new CacheStats();

    /**
     * 各缓存区域的统计
     */
    private final ConcurrentHashMap<String, CacheStats> cacheStatsMap = new ConcurrentHashMap<>();

    /**
     * 记录缓存命中
     *
     * @param cacheName 缓存名称
     */
    public void recordHit(String cacheName) {
        globalStats.recordHit();
        getCacheStats(cacheName).recordHit();
    }

    /**
     * 记录缓存未命中
     *
     * @param cacheName 缓存名称
     */
    public void recordMiss(String cacheName) {
        globalStats.recordMiss();
        getCacheStats(cacheName).recordMiss();
    }

    /**
     * 记录缓存写入
     *
     * @param cacheName 缓存名称
     */
    public void recordPut(String cacheName) {
        globalStats.recordPut();
        getCacheStats(cacheName).recordPut();
    }

    /**
     * 记录缓存驱逐
     *
     * @param cacheName 缓存名称
     */
    public void recordEvict(String cacheName) {
        globalStats.recordEvict();
        getCacheStats(cacheName).recordEvict();
    }

    /**
     * 记录缓存响应时间
     *
     * @param cacheName 缓存名称
     * @param milliseconds 响应时间（毫秒）
     */
    public void recordResponse(String cacheName, long milliseconds) {
        globalStats.recordResponse(milliseconds);
        getCacheStats(cacheName).recordResponse(milliseconds);
    }

    /**
     * 获取指定缓存的统计信息
     *
     * @param cacheName 缓存名称
     * @return 缓存统计信息
     */
    public CacheStats getCacheStats(String cacheName) {
        return cacheStatsMap.computeIfAbsent(cacheName, k -> new CacheStats());
    }

    /**
     * 获取全局统计信息
     *
     * @return 全局缓存统计
     */
    public CacheStats getGlobalStats() {
        return globalStats;
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        globalStats.hitCount.reset();
        globalStats.missCount.reset();
        globalStats.putCount.reset();
        globalStats.evictCount.reset();
        globalStats.totalResponseTime.set(0);

        cacheStatsMap.clear();
        log.info("缓存监控统计已重置");
    }

    /**
     * 重置指定缓存的统计信息
     *
     * @param cacheName 缓存名称
     */
    public void reset(String cacheName) {
        CacheStats stats = cacheStatsMap.get(cacheName);
        if (stats != null) {
            stats.hitCount.reset();
            stats.missCount.reset();
            stats.putCount.reset();
            stats.evictCount.reset();
            stats.totalResponseTime.set(0);
            log.info("缓存监控统计已重置: cacheName={}", cacheName);
        }
    }

    /**
     * 获取监控报告
     *
     * @return 监控报告文本
     */
    public String getReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n========== 缓存监控报告 ==========\n");
        report.append(String.format("全局统计: %s\n", globalStats.toString()));
        report.append("--------------------------------------\n");
        report.append("各缓存区域统计:\n");

        cacheStatsMap.forEach((cacheName, stats) -> {
            report.append(String.format("  [%s] %s\n", cacheName, stats.toString()));
        });

        report.append("======================================\n");
        return report.toString();
    }

    /**
     * 打印监控报告到日志
     */
    public void logReport() {
        log.info(getReport());
    }
}
