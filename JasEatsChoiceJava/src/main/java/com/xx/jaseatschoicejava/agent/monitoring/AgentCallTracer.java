package com.xx.jaseatschoicejava.agent.monitoring;

import dev.langchain4j.observability.api.event.AiServiceEvent;
import dev.langchain4j.observability.api.event.AiServiceStartedEvent;
import dev.langchain4j.observability.api.event.AiServiceRequestIssuedEvent;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agent调用链追踪监听器
 * 记录每次Agent调用的完整路径和参数
 *
 * @author Claude
 * @since 2026-03-25
 */
public class AgentCallTracer implements AiServiceListener<AiServiceEvent> {

    private static final Logger log = LoggerFactory.getLogger(AgentCallTracer.class);
    private static final AtomicInteger callCounter = new AtomicInteger(0);

    @Override
    public Class<AiServiceEvent> getEventClass() {
        return AiServiceEvent.class;
    }

    @Override
    public void onEvent(AiServiceEvent event) {
        int callId = callCounter.incrementAndGet();

        if (event instanceof AiServiceStartedEvent) {
            onAgentStarted((AiServiceStartedEvent) event, callId);
        } else if (event instanceof AiServiceRequestIssuedEvent) {
            onRequestIssued((AiServiceRequestIssuedEvent) event, callId);
        } else if (event instanceof AiServiceResponseReceivedEvent) {
            onResponseReceived((AiServiceResponseReceivedEvent) event, callId);
        } else if (event instanceof AiServiceCompletedEvent) {
            onAgentCompleted((AiServiceCompletedEvent) event, callId);
        }
    }

    /**
     * Agent调用开始
     */
    private void onAgentStarted(AiServiceStartedEvent event, int callId) {
        String userMessage = event.userMessage().singleText();
        log.info("🤖 [Agent调用开始 #{}] 用户消息: {}", callId, truncate(userMessage, 100));
    }

    /**
     * 请求发送到LLM
     */
    private void onRequestIssued(AiServiceRequestIssuedEvent event, int callId) {
        log.debug("📤 [请求发出 #{}] 发送到LLM模型", callId);
    }

    /**
     * LLM响应接收
     */
    private void onResponseReceived(AiServiceResponseReceivedEvent event, int callId) {
        log.debug("📥 [响应接收 #{}] LLM响应接收", callId);
    }

    /**
     * Agent调用完成
     */
    private void onAgentCompleted(AiServiceCompletedEvent event, int callId) {
        log.info("✅ [Agent调用完成 #{}] Agent处理完成", callId);
    }

    /**
     * 截断过长的文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "null";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
