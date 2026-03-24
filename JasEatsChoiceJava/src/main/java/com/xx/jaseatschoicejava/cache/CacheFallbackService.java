package com.xx.jaseatschoicejava.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 缓存降级组件
 *
 * 功能：
 * 1. Redis健康检查
 * 2. 自动降级到数据库
 * 3. 半开模式（自动恢复）
 * 4. 熔断机制
 *
 * 降级策略：
 * - Redis连接失败：自动降级到数据库
 * - Redis超时：自动降级到数据库
 * - 连续失败达到阈值：开启熔断，直接查数据库
 * - 半开模式：定期尝试恢复Redis连接
 *
 * @author Claude Code
 * @since 2026-03-24
 */
@Slf4j
@Component
public class CacheFallbackService {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 熔断器状态
     */
    private enum CircuitBreakerState {
        CLOSED,    // 关闭（正常，可以使用Redis）
        OPEN,      // 开启（熔断，直接查数据库）
        HALF_OPEN  // 半开（尝试恢复Redis）
    }

    /**
     * 当前熔断器状态
     */
    private volatile CircuitBreakerState circuitBreakerState = CircuitBreakerState.CLOSED;

    /**
     * 连续失败计数
     */
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * 连续成功计数
     */
    private final AtomicInteger successCount = new AtomicInteger(0);

    /**
     * 熔断阈值（连续失败多少次后开启熔断）
     */
    private static final int FAILURE_THRESHOLD = 5;

    /**
     * 恢复阈值（连续成功多少次后关闭熔断）
     */
    private static final int RECOVERY_THRESHOLD = 3;

    /**
     * 半开模式重试间隔（毫秒）
     */
    private static final long HALF_OPEN_RETRY_INTERVAL = 30000; // 30秒

    /**
     * 最后一次失败时间
     */
    private volatile long lastFailureTime;

    /**
     * 最后一次成功时间
     */
    private volatile long lastSuccessTime;

    /**
     * 是否启用降级（默认启用）
     */
    private volatile boolean fallbackEnabled = true;

    /**
     * 在缓存保护下执行操作
     *
     * @param operation 要执行的操作
     * @param fallback 降级操作（Redis不可用时执行）
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithFallback(Supplier<T> operation, Supplier<T> fallback) {
        return executeWithFallback(operation, fallback, true);
    }

    /**
     * 在缓存保护下执行操作
     *
     * @param operation 要执行的操作
     * @param fallback 降级操作（Redis不可用时执行）
     * @param useFallback 是否使用降级
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithFallback(Supplier<T> operation, Supplier<T> fallback, boolean useFallback) {
        // 检查是否启用降级
        if (!useFallback || !fallbackEnabled) {
            return operation.get();
        }

        // 检查熔断器状态
        if (circuitBreakerState == CircuitBreakerState.OPEN) {
            // 熔断器开启，直接降级
            log.warn("熔断器已开启，直接使用降级策略");
            return fallback.get();
        }

        try {
            // 尝试执行操作
            T result = operation.get();

            // 操作成功，记录成功
            recordSuccess();

            return result;

        } catch (Exception e) {
            // 操作失败，记录失败
            recordFailure();

            // 判断是否需要降级
            if (shouldFallback()) {
                log.warn("Redis操作失败，使用降级策略: error={}", e.getMessage());
                return fallback.get();
            } else {
                // 不降级，抛出异常
                throw e;
            }
        }
    }

    /**
     * 检查Redis健康状态
     *
     * @return true-健康，false-不健康
     */
    public boolean isRedisHealthy() {
        try {
            // 尝试获取Redis连接
            RedisConnection connection = redisConnectionFactory.getConnection();

            // 简单ping测试
            String ping = connection.ping();
            connection.close();

            boolean healthy = "PONG".equalsIgnoreCase(ping);

            if (healthy) {
                recordSuccess();
            } else {
                recordFailure();
            }

            return healthy;

        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            recordFailure();
            return false;
        }
    }

    /**
     * 记录成功
     */
    private void recordSuccess() {
        successCount.incrementAndGet();
        failureCount.set(0);
        lastSuccessTime = System.currentTimeMillis();

        // 如果在半开状态，连续成功达到阈值，则关闭熔断器
        if (circuitBreakerState == CircuitBreakerState.HALF_OPEN) {
            if (successCount.get() >= RECOVERY_THRESHOLD) {
                log.info("Redis已恢复，关闭熔断器");
                circuitBreakerState = CircuitBreakerState.CLOSED;
            }
        }
    }

    /**
     * 记录失败
     */
    private void recordFailure() {
        failureCount.incrementAndGet();
        successCount.set(0);
        lastFailureTime = System.currentTimeMillis();

        // 连续失败达到阈值，开启熔断器
        if (failureCount.get() >= FAILURE_THRESHOLD) {
            log.error("Redis连续失败{}次，开启熔断器", FAILURE_THRESHOLD);
            circuitBreakerState = CircuitBreakerState.OPEN;

            // 30秒后进入半开模式
            new Thread(() -> {
                try {
                    Thread.sleep(HALF_OPEN_RETRY_INTERVAL);
                    log.info("进入半开模式，尝试恢复Redis连接");
                    circuitBreakerState = CircuitBreakerState.HALF_OPEN;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * 判断是否应该降级
     *
     * @return true-应该降级，false-不应该降级
     */
    private boolean shouldFallback() {
        // 检查连续失败次数
        if (failureCount.get() >= 3) {
            return true;
        }

        // 检查熔断器状态
        if (circuitBreakerState == CircuitBreakerState.OPEN) {
            return true;
        }

        return false;
    }

    /**
     * 手动重置熔断器
     */
    public void resetCircuitBreaker() {
        log.info("手动重置熔断器状态");
        circuitBreakerState = CircuitBreakerState.CLOSED;
        failureCount.set(0);
        successCount.set(0);
    }

    /**
     * 启用降级
     */
    public void enableFallback() {
        log.info("启用缓存降级");
        this.fallbackEnabled = true;
    }

    /**
     * 禁用降级
     */
    public void disableFallback() {
        log.info("禁用缓存降级");
        this.fallbackEnabled = false;
    }

    /**
     * 获取熔断器状态
     *
     * @return 熔断器状态
     */
    public String getCircuitBreakerState() {
        return circuitBreakerState.name();
    }

    /**
     * 获取统计信息
     *
     * @return 统计信息
     */
    public String getStats() {
        return String.format(
            "CacheFallback Stats: state=%s, failureCount=%d, successCount=%d, fallbackEnabled=%s",
            circuitBreakerState.name(),
            failureCount.get(),
            successCount.get(),
            fallbackEnabled
        );
    }

    /**
     * 尝试恢复Redis连接（用于半开模式）
     *
     * @return true-恢复成功，false-恢复失败
     */
    public boolean tryRecover() {
        log.info("尝试恢复Redis连接...");

        if (isRedisHealthy()) {
            log.info("Redis连接已恢复");
            return true;
        } else {
            log.warn("Redis连接恢复失败");
            return false;
        }
    }

    /**
     * 等待Redis恢复（阻塞等待）
     *
     * @param maxWaitTime 最大等待时间（毫秒）
     * @param checkInterval 检查间隔（毫秒）
     * @return true-恢复成功，false-超时
     */
    public boolean waitForRecovery(long maxWaitTime, long checkInterval) {
        log.info("等待Redis恢复: maxWaitTime={}ms, checkInterval={}ms", maxWaitTime, checkInterval);

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < maxWaitTime) {
            if (isRedisHealthy()) {
                log.info("Redis已恢复");
                return true;
            }

            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待Redis恢复被中断");
                return false;
            }
        }

        log.warn("等待Redis恢复超时");
        return false;
    }

    /**
     * 快速失败（当Redis不可用时直接返回null）
     *
     * @param supplier 数据加载函数
     * @param <T> 返回值类型
     * @return 数据或null
     */
    public <T> T failFast(Supplier<T> supplier) {
        if (!isRedisHealthy()) {
            log.warn("Redis不健康，快速失败");
            return null;
        }

        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("数据加载失败", e);
            return null;
        }
    }

    /**
     * 带重试的操作
     *
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param retryDelay 重试延迟（毫秒）
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithRetry(Supplier<T> operation, int maxRetries, long retryDelay) {
        Exception lastException = null;

        for (int i = 0; i <= maxRetries; i++) {
            try {
                if (i > 0) {
                    log.debug("重试操作: attempt={}", i);
                }

                return operation.get();

            } catch (Exception e) {
                lastException = e;

                if (i < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("操作失败，已重试{}次", maxRetries, lastException);
        throw new RuntimeException("操作失败: " + lastException.getMessage(), lastException);
    }
}
