package com.xx.jaseatschoicejava.agent.monitoring;

import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * Agent监控配置类
 * 配置各类监听器，实现Agent调用链追踪和性能监控
 *
 * @author Claude
 * @since 2026-03-25
 */
@Configuration
@ConditionalOnProperty(
    name = "agent.monitoring.enabled",
    havingValue = "true",
    matchIfMissing = false  // 默认禁用监控，避免配置问题影响启动
)
public class AgentMonitoringConfig {

    private static final Logger log = LoggerFactory.getLogger(AgentMonitoringConfig.class);

    private AiServiceListenerRegistrar listenerRegistrar;
    private AgentCallTracer agentCallTracer;
    private AgentPerformanceMonitor agentPerformanceMonitor;

    /**
     * 配置AI服务监听器注册器
     * 自动注册所有自定义监听器到AiServices
     */
    @Bean
    public AiServiceListenerRegistrar aiServiceListenerRegistrar() {
        log.info("初始化Agent监控监听器...");

        this.listenerRegistrar = AiServiceListenerRegistrar.newInstance();
        this.agentCallTracer = new AgentCallTracer();
        this.agentPerformanceMonitor = new AgentPerformanceMonitor();

        // 注册监听器
        listenerRegistrar.register(agentCallTracer);
        listenerRegistrar.register(agentPerformanceMonitor);

        log.info("✅ Agent监控监听器注册完成");
        return listenerRegistrar;
    }

    /**
     * 获取性能统计信息
     */
    @Bean
    public AgentPerformanceMonitor.PerformanceStats performanceStats() {
        return agentPerformanceMonitor.getStats();
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void cleanup() {
        if (listenerRegistrar != null) {
            listenerRegistrar.unregister(agentCallTracer);
            listenerRegistrar.unregister(agentPerformanceMonitor);
            log.info("Agent监控监听器已注销");
        }
    }
}
