package com.xx.jaseatschoicejava.agent.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 调用链追踪服务
 * 记录和生成Agent调用的完整链路
 *
 * @author Claude
 * @since 2026-03-25
 */
@Service
public class CallChainTraceService {

    private static final Logger log = LoggerFactory.getLogger(CallChainTraceService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // 存储每个会话的调用链
    private final ConcurrentHashMap<String, CallChain> callChains = new ConcurrentHashMap<>();

    // 会话ID生成器
    private final AtomicInteger sessionIdGenerator = new AtomicInteger(0);

    /**
     * 创建新的调用链
     */
    public CallChain createCallChain(String userId, String userMessage) {
        String sessionId = "session-" + sessionIdGenerator.incrementAndGet();
        CallChain chain = new CallChain(sessionId, userId, userMessage);
        callChains.put(sessionId, chain);
        log.info("📝 创建调用链: {} | 用户: {} | 消息: {}",
                sessionId, userId, truncate(userMessage, 50));
        return chain;
    }

    /**
     * 记录Agent调用
     */
    public void recordAgentCall(String sessionId, String agentName, Object... params) {
        CallChain chain = callChains.get(sessionId);
        if (chain != null) {
            chain.addAgentCall(agentName, params);
        }
    }

    /**
     * 记录工具调用
     */
    public void recordToolCall(String sessionId, String toolName, Object result) {
        CallChain chain = callChains.get(sessionId);
        if (chain != null) {
            chain.addToolCall(toolName, result);
        }
    }

    /**
     * 完成调用链
     */
    public void completeCallChain(String sessionId, String finalResponse) {
        CallChain chain = callChains.get(sessionId);
        if (chain != null) {
            chain.complete(finalResponse);
            log.info("✅ 调用链完成: {} | 总耗时: {}ms | Agent调用: {}次 | 工具调用: {}次",
                    sessionId,
                    chain.getTotalDuration(),
                    chain.getAgentCalls().size(),
                    chain.getToolCalls().size());
        }
    }

    /**
     * 生成调用链报告
     */
    public String generateReport(String sessionId) {
        CallChain chain = callChains.get(sessionId);
        if (chain == null) {
            return "调用链不存在: " + sessionId;
        }

        StringBuilder report = new StringBuilder();
        report.append("\n");
        report.append("=" .repeat(80)).append("\n");
        report.append("📊 Agent调用链报告").append("\n");
        report.append("=" .repeat(80)).append("\n");
        report.append(String.format("会话ID: %s\n", chain.getSessionId()));
        report.append(String.format("用户: %s\n", chain.getUserId()));
        report.append(String.format("开始时间: %s\n", chain.getStartTime().format(TIME_FORMATTER)));
        report.append(String.format("结束时间: %s\n", chain.getEndTime().format(TIME_FORMATTER)));
        report.append(String.format("总耗时: %dms\n", chain.getTotalDuration()));
        report.append("\n");

        // Agent调用链
        report.append("🤖 Agent调用链:\n");
        report.append("-" .repeat(80)).append("\n");
        for (int i = 0; i < chain.getAgentCalls().size(); i++) {
            AgentCall call = chain.getAgentCalls().get(i);
            report.append(String.format("%d. [%s] %s\n",
                    i + 1,
                    call.getTimestamp().format(TIME_FORMATTER),
                    call.getAgentName()));
            if (call.getParams() != null && call.getParams().length > 0) {
                report.append("   参数: ");
                for (Object param : call.getParams()) {
                    report.append(truncate(String.valueOf(param), 30)).append(" ");
                }
                report.append("\n");
            }
        }
        report.append("\n");

        // 工具调用列表
        if (!chain.getToolCalls().isEmpty()) {
            report.append("🔧 工具调用列表:\n");
            report.append("-" .repeat(80)).append("\n");
            for (int i = 0; i < chain.getToolCalls().size(); i++) {
                ToolCall call = chain.getToolCalls().get(i);
                report.append(String.format("%d. [%s] %s\n",
                        i + 1,
                        call.getTimestamp().format(TIME_FORMATTER),
                        call.getToolName()));
            }
            report.append("\n");
        }

        // 最终响应
        report.append("💬 最终响应:\n");
        report.append("-" .repeat(80)).append("\n");
        report.append(truncate(chain.getFinalResponse(), 200)).append("\n");
        report.append("\n");
        report.append("=" .repeat(80)).append("\n");

        return report.toString();
    }

    /**
     * 清理过期的调用链
     */
    public void cleanupOldChains(int maxAgeMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(maxAgeMinutes);
        callChains.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().getStartTime().isBefore(cutoff);
            if (shouldRemove) {
                log.debug("🗑️ 清理过期调用链: {}", entry.getKey());
            }
            return shouldRemove;
        });
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

    /**
     * 调用链
     */
    public static class CallChain {
        private final String sessionId;
        private final String userId;
        private final String userMessage;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private final List<AgentCall> agentCalls = new ArrayList<>();
        private final List<ToolCall> toolCalls = new ArrayList<>();
        private String finalResponse;

        public CallChain(String sessionId, String userId, String userMessage) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.userMessage = userMessage;
            this.startTime = LocalDateTime.now();
        }

        public void addAgentCall(String agentName, Object[] params) {
            agentCalls.add(new AgentCall(agentName, params));
        }

        public void addToolCall(String toolName, Object result) {
            toolCalls.add(new ToolCall(toolName, result));
        }

        public void complete(String finalResponse) {
            this.endTime = LocalDateTime.now();
            this.finalResponse = finalResponse;
        }

        public long getTotalDuration() {
            return endTime != null ?
                java.time.Duration.between(startTime, endTime).toMillis() : 0;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public String getUserMessage() { return userMessage; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public List<AgentCall> getAgentCalls() { return agentCalls; }
        public List<ToolCall> getToolCalls() { return toolCalls; }
        public String getFinalResponse() { return finalResponse; }
    }

    /**
     * Agent调用记录
     */
    public static class AgentCall {
        private final String agentName;
        private final Object[] params;
        private final LocalDateTime timestamp = LocalDateTime.now();

        public AgentCall(String agentName, Object[] params) {
            this.agentName = agentName;
            this.params = params;
        }

        public String getAgentName() { return agentName; }
        public Object[] getParams() { return params; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 工具调用记录
     */
    public static class ToolCall {
        private final String toolName;
        private final Object result;
        private final LocalDateTime timestamp = LocalDateTime.now();

        public ToolCall(String toolName, Object result) {
            this.toolName = toolName;
            this.result = result;
        }

        public String getToolName() { return toolName; }
        public Object getResult() { return result; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
