package com.xx.jaseatschoicejava.agent.monitoring;

import dev.langchain4j.observability.api.event.AiServiceEvent;
import dev.langchain4j.observability.api.event.AiServiceStartedEvent;
import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Agent性能监控监听器
 * 记录每次Agent调用的耗时和性能指标
 *
 * @author Claude
 * @since 2026-03-25
 */
public class AgentPerformanceMonitor implements AiServiceListener<AiServiceEvent> {

    private static final Logger log = LoggerFactory.getLogger(AgentPerformanceMonitor.class);

    // 存储每次调用的开始时间
    private final ConcurrentHashMap<Long, LocalDateTime> startTimes = new ConcurrentHashMap<>();

    // 性能统计
    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    private final AtomicLong maxDuration = new AtomicLong(0);

    @Override
    public Class<AiServiceEvent> getEventClass() {
        return AiServiceEvent.class;
    }

    @Override
    public void onEvent(AiServiceEvent event) {
        if (event instanceof AiServiceStartedEvent) {
            onAgentStarted((AiServiceStartedEvent) event);
        } else if (event instanceof AiServiceCompletedEvent) {
            onAgentCompleted((AiServiceCompletedEvent) event);
        }
    }

    /**
     * Agent调用开始 - 记录开始时间
     */
    private void onAgentStarted(AiServiceStartedEvent event) {
        long callId = totalCalls.incrementAndGet();
        startTimes.put(callId, LocalDateTime.now());
        log.debug("⏱️ [性能监控 #{}] Agent调用开始", callId);
    }

    /**
     * Agent调用完成 - 计算耗时并记录
     */
    private void onAgentCompleted(AiServiceCompletedEvent event) {
        long callId = totalCalls.get();
        LocalDateTime startTime = startTimes.remove(callId);

        if (startTime != null) {
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            long durationMs = duration.toMillis();

            // 更新统计数据
            totalDuration.addAndGet(durationMs);
            updateMaxDuration(durationMs);

            // 计算平均耗时
            long avgDuration = totalDuration.get() / totalCalls.get();

            log.info("⏱️ [性能监控 #{}] 耗时: {}ms | 平均: {}ms | 最大: {}ms | 总调用: {}",
                    callId,
                    durationMs,
                    avgDuration,
                    maxDuration.get(),
                    totalCalls.get());

            // 性能警告
            if (durationMs > 5000) {
                log.warn("⚠️ [性能警告] Agent调用耗时过长: {}ms", durationMs);
            }
        }
    }

    /**
     * 更新最大耗时
     */
    private void updateMaxDuration(long duration) {
        long current;
        long updated;
        do {
            current = maxDuration.get();
            updated = Math.max(current, duration);
        } while (!maxDuration.compareAndSet(current, updated));
    }

    /**
     * 获取性能统计信息
     */
    public PerformanceStats getStats() {
        long total = totalCalls.get();
        long avg = total > 0 ? totalDuration.get() / total : 0;

        return new PerformanceStats(
                total,
                avg,
                maxDuration.get(),
                totalDuration.get()
        );
    }

    /**
     * 性能统计信息
     */
    public record PerformanceStats(
            long totalCalls,      // 总调用次数
            long averageDuration, // 平均耗时(ms)
            long maxDuration,     // 最大耗时(ms)
            long totalDuration    // 总耗时(ms)
    ) {}
}
