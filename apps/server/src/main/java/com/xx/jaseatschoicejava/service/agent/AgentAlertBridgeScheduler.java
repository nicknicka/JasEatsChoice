package com.xx.jaseatschoicejava.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 只读轮询调度器。默认关闭（agent.bridge.enabled=false），需要时显式打开。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "agent.bridge.enabled", havingValue = "true")
public class AgentAlertBridgeScheduler {

    private final AgentAlertBridge bridge;

    public AgentAlertBridgeScheduler(AgentAlertBridge bridge) {
        this.bridge = bridge;
    }

    @Scheduled(fixedDelayString = "${agent.bridge.poll-interval-ms:60000}")
    public void poll() {
        try {
            int ingested = bridge.pollOnce();
            if (ingested > 0) {
                log.info("Agent Alert Bridge 采集到 {} 个新事件", ingested);
            }
        } catch (Exception e) {
            // 轮询失败只记日志，下一次再试；不得影响应用其它功能。
            log.warn("Agent Alert Bridge 轮询失败：{}", e.toString());
        }
    }
}
