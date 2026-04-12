package com.xx.jaseatschoicejava.agent.listener;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agentic.observability.AfterAgentToolExecution;
import dev.langchain4j.agentic.observability.AgentInvocationError;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.observability.AgentRequest;
import dev.langchain4j.agentic.observability.AgentResponse;
import dev.langchain4j.agentic.observability.BeforeAgentToolExecution;
import dev.langchain4j.agentic.scope.AgenticScope;

/**
 * SSE Agent 执行监听器
 *
 * 实时捕获SupervisorAgent的执行步骤，通过SSE推送到前端
 *
 * @author Claude
 * @since 2026-03-26
 */
public class SSEAgentListener implements AgentListener {

    private static final Logger log = LoggerFactory.getLogger(SSEAgentListener.class);

    private final SseEmitter emitter;
    private final ObjectMapper objectMapper;
    private final String userId;
    private final String originalUserMessage;
    private volatile boolean emitterFailed = false;

    // 去重：记录已发送的事件签名（agentId + eventType），防止重复推送
    private final Set<String> sentEventKeys = new HashSet<>();

    public SSEAgentListener(SseEmitter emitter, String userId, String originalUserMessage) {
        this.emitter = emitter;
        this.userId = userId;
        this.originalUserMessage = originalUserMessage;
        this.objectMapper = new ObjectMapper();
    }

    public SseEmitter getEmitter() {
        return emitter;
    }

    // 耗时追踪
    private long agentStartTime;
    private long toolStartTime;

    @Override
    public void beforeAgentInvocation(AgentRequest request) {
        String agentName = request.agentName();
        agentStartTime = System.currentTimeMillis();

        // ========== 【技术细节】只记录到日志 ==========
        log.info("⏱️ [耗时追踪] Agent调用开始: {}, 时间={}", agentName, agentStartTime);

        // ========== 【用户友好进度】发送可理解的进度消息 ==========
        String userFriendlyMessage = getUserFriendlyProgressMessage(agentName, true);

        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName(agentName);
        event.setAgentId(firstNonBlank(
            extractPropertyAsString(request, "agentId", "id"),
            agentName
        ));
        event.setInputs(firstNonBlank(
            extractPropertyAsString(request, "inputs", "input", "arguments", "parameters", "message", "userMessage", "content"),
            request.toString()
        ));
        event.setMessage(userFriendlyMessage);
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息，不保存到数据库

        sendEvent(ExecutionEventType.AGENT_START, event);
    }

    @Override
    public void afterAgentInvocation(AgentResponse response) {
        String agentName = response.agentName();
        long duration = agentStartTime > 0 ? System.currentTimeMillis() - agentStartTime : -1;

        // ========== 【技术细节】只记录到日志 ==========
        log.info("⏱️ [耗时追踪] Agent调用完成: {}, 耗时={}ms", agentName, duration);

        // ========== 【用户友好进度】发送完成消息 ==========
        String userFriendlyMessage = getUserFriendlyProgressMessage(agentName, false);

        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName(agentName);
        event.setAgentId(firstNonBlank(
            extractPropertyAsString(response, "agentId", "id"),
            agentName
        ));
        event.setOutput(firstNonBlank(
            extractPropertyAsString(response, "output", "result", "value", "content"),
            response.toString()
        ));
        event.setMessage(userFriendlyMessage);
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.AGENT_COMPLETE, event);
    }

    @Override
    public void onAgentInvocationError(AgentInvocationError error) {
        String agentName = error.agentName();

        // ========== 【技术细节】只记录到日志 ==========
        log.error("❌ [技术细节] Agent调用失败: {}", agentName);

        // ========== 【用户友好消息】发送简化的错误提示 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName(agentName);
        event.setAgentId(firstNonBlank(agentName, error.agentName()));
        event.setError(firstNonBlank(extractPropertyAsString(error, "message", "detail"), error.toString()));
        event.setMessage("处理过程中遇到问题，请稍后重试...");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.AGENT_ERROR, event);
    }

    @Override
    public void beforeAgentToolExecution(BeforeAgentToolExecution execution) {
        toolStartTime = System.currentTimeMillis();
        // ========== 【技术细节】只记录到日志 ==========
        log.info("⏱️ [耗时追踪] 工具执行开始: {}", execution);

        // ========== 【用户友好进度】发送工具执行消息 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setToolName(firstNonBlank(
            extractPropertyAsString(execution, "toolName", "name", "tool"),
            execution.toString()
        ));
        event.setInputs(firstNonBlank(
            extractPropertyAsString(execution, "inputs", "input", "arguments", "parameters", "args"),
            execution.toString()
        ));
        event.setMessage("正在查询数据");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.TOOL_START, event);
    }

    @Override
    public void afterAgentToolExecution(AfterAgentToolExecution execution) {
        long duration = toolStartTime > 0 ? System.currentTimeMillis() - toolStartTime : -1;
        // ========== 【技术细节】只记录到日志 ==========
        log.info("⏱️ [耗时追踪] 工具执行完成: {}, 耗时={}ms", execution, duration);

        // ========== 【用户友好进度】发送工具完成消息 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setToolName(firstNonBlank(
            extractPropertyAsString(execution, "toolName", "name", "tool"),
            execution.toString()
        ));
        event.setOutput(firstNonBlank(
            extractPropertyAsString(execution, "output", "result", "response", "value"),
            execution.toString()
        ));
        event.setMessage("数据查询完成");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.TOOL_COMPLETE, event);
    }

    @Override
    public void afterAgenticScopeCreated(AgenticScope scope) {
        // ========== 【技术细节】只记录到日志 ==========
        log.info("🎯 [技术细节] AgenticScope创建: {}", scope.memoryId());

        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName("SupervisorAgent");
        event.setAgentId(String.valueOf(scope.memoryId()));

        // 将userId写入AgenticScope，供所有L1子Agent的工具读取
        if (userId != null && !userId.isEmpty()) {
            scope.writeState("userId", userId);
            log.info("🔑 [AgenticScope] 已写入userId: {}", userId);
        }

        if (originalUserMessage != null && !originalUserMessage.isEmpty()) {
            scope.writeState("originalUserMessage", originalUserMessage);
            scope.writeState("preferenceWriteAllowed", String.valueOf(isExplicitPreferenceUpdateRequest(originalUserMessage)));
        }

        // ========== 【用户友好消息】发送任务开始提示 ==========
        event.setMessage("正在为您处理...");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.INIT, event);
    }

    @Override
    public void beforeAgenticScopeDestroyed(AgenticScope scope) {
        // ========== 【技术细节】只记录到日志 ==========
        log.info("🏁 [技术细节] AgenticScope销毁: {}", scope.memoryId());

        // ========== 【用户友好消息】发送完成标记 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName("SupervisorAgent");
        event.setAgentId(String.valueOf(scope.memoryId()));
        event.setMessage("处理完成");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);   // 标记为进度消息
        event.setCompleted(true);  // 标记为完成，前端应隐藏进度指示器

        sendEvent(ExecutionEventType.FINISH, event);
    }

    /**
     * 获取用户友好的进度消息
     *
     * @param agentName Agent名称
     * @param isStart 是否为开始阶段
     * @return 用户友好的进度描述
     */
    private String getUserFriendlyProgressMessage(String agentName, boolean isStart) {
        // 提取简单的agent名称（去掉$0、$1等后缀）
        String simpleName = agentName.replaceAll("\\$\\d+", "");

        return switch (simpleName) {
            case "DishRecommendationAgent" -> isStart ? "正在为您搜索菜品" : "菜品搜索完成";
            case "UserPreferenceAgent" -> isStart ? "正在分析您的偏好" : "偏好分析完成";
            case "NutritionGuideAgent" -> isStart ? "正在分析营养成分" : "营养分析完成";
            case "OrderHelperAgent" -> isStart ? "正在处理订单" : "订单处理完成";
            case "MerchantInfoAgent" -> isStart ? "正在查询商家信息" : "商家信息查询完成";
            case "TimeAwareAgent" -> isStart ? "正在分析时段推荐" : "时段分析完成";
            case "LocationServiceAgent" -> isStart ? "正在查询位置服务" : "位置服务查询完成";
            case "SupervisorAgent" -> isStart ? "正在为您分析需求" : "需求分析完成";
            default -> isStart ? "正在处理中" : "处理完成";
        };
    }

    private String extractPropertyAsString(Object source, String... propertyNames) {
        if (source == null || propertyNames == null || propertyNames.length == 0) {
            return null;
        }

        for (String propertyName : propertyNames) {
            Object value = readProperty(source, propertyName);
            if (value == null) {
                continue;
            }

            String text = normalizeValue(value);
            if (text != null && !text.isBlank()) {
                return text;
            }
        }

        return null;
    }

    private Object readProperty(Object source, String propertyName) {
        Class<?> sourceClass = source.getClass();
        String capitalized = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

        try {
            Method getter = sourceClass.getMethod("get" + capitalized);
            return getter.invoke(source);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }

        try {
            Method getter = sourceClass.getMethod(propertyName);
            return getter.invoke(source);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }

        try {
            Field field = sourceClass.getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(source);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return null;
    }

    private String normalizeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String stringValue) {
            return stringValue;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ignored) {
            return value.toString();
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public boolean inheritedBySubagents() {
        // 不继承到子Agent，避免 Supervisor 层和子 Agent 层重复触发同一事件
        return false;
    }

    private boolean isExplicitPreferenceUpdateRequest(String message) {
        if (message == null) {
            return false;
        }

        String normalized = message.replaceAll("\\s+", "");
        return normalized.contains("修改偏好")
                || normalized.contains("更新偏好")
                || normalized.contains("设置偏好")
                || normalized.contains("调整偏好")
                || normalized.contains("修改忌口")
                || normalized.contains("设置忌口")
                || normalized.contains("更新忌口")
                || normalized.contains("修改过敏")
                || normalized.contains("添加过敏")
                || normalized.contains("更新资料");
    }

    /**
     * 发送SSE事件
     *
     * ⚠️ 重要：事件名必须使用 "message"，因为前端只监听 message 事件
     */
    private void sendEvent(ExecutionEventType type, ExecutionEvent event) {
        // 如果 emitter 已失败，跳过后续所有发送，避免级联错误
        if (emitterFailed) {
            log.debug("⏭️ [SSE] 跳过发送（emitter已失败）: type={}", type.name());
            return;
        }

        // 去重保护：同一 agentId + eventType 只发送一次
        String eventKey = event.getAgentId() + ":" + type.name();
        if (!sentEventKeys.add(eventKey)) {
            log.debug("⏭️ [SSE] 跳过重复事件: key={}", eventKey);
            return;
        }

        event.setEventType(type.name());

        long startTime = System.currentTimeMillis();
        try {
            String eventData = objectMapper.writeValueAsString(event);

            // ========== 【详细日志】降为DEBUG，减少生产环境噪音 ==========
            log.debug("==================== SSE事件发送开始 ====================");
            log.debug("📤 [SSE] 事件类型: {}", type.name());
            log.debug("📤 [SSE] 事件时间: {}", new java.util.Date());
            log.debug("📤 [SSE] 数据长度: {} 字符", eventData.length());
            log.debug("📤 [SSE] 完整数据:");
            log.debug("─ 开始 ({} 字符) ─", eventData.length());
            log.debug(eventData);
            log.debug("─ 结束 ─");
            log.debug("📤 [SSE] Event对象详情:");
            log.debug("   - agentName: {}", event.getAgentName());
            log.debug("   - message: {}", event.getMessage());
            log.debug("   - isProgress: {}", event.isProgress());
            log.debug("   - timestamp: {}", event.getTimestamp());
            log.debug("   - toolName: {}", event.getToolName());
            log.debug("   - inputs: {}", event.getInputs() != null ? event.getInputs().substring(0, Math.min(100, event.getInputs().length())) + "..." : "null");
            log.debug("   - output: {}", event.getOutput() != null ? event.getOutput().substring(0, Math.min(100, event.getOutput().length())) + "..." : "null");
            log.debug("=====================================================");

            // ✅ 统一使用 "message" 事件名，前端才能接收
            emitter.send(SseEmitter.event()
                    .name("message")  // 固定使用message事件名
                    .data(eventData));

            long duration = System.currentTimeMillis() - startTime;
            log.debug("✅ [SSE] 事件发送成功: type={}, 耗时={}ms", type.name(), duration);
        } catch (IOException e) {
            emitterFailed = true;
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ [SSE] 发送SSE事件失败: type={}, 耗时={}ms, error={}", type.name(), duration, e.getMessage());
            // 不调用 emitter.completeWithError()，避免级联 IllegalStateException
            // emitter 的生命周期由 SupervisorSSEController 统一管理
        } catch (Exception e) {
            emitterFailed = true;
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ [SSE] 发送SSE事件异常: type={}, 耗时={}ms, error={}", type.name(), duration, e.getMessage());
            // 不调用 emitter.completeWithError()，避免级联 IllegalStateException
        }
    }

}


