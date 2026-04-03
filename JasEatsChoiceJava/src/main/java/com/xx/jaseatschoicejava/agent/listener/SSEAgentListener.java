package com.xx.jaseatschoicejava.agent.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agentic.observability.*;
import dev.langchain4j.agentic.scope.AgenticScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

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
    private volatile boolean emitterFailed = false;

    public SSEAgentListener(SseEmitter emitter, String userId) {
        this.emitter = emitter;
        this.userId = userId;
        this.objectMapper = new ObjectMapper();
    }

    public SseEmitter getEmitter() {
        return emitter;
    }

    @Override
    public void beforeAgentInvocation(AgentRequest request) {
        String agentName = request.agentName();

        // ========== 【技术细节】只记录到日志 ==========
        log.debug("🔧 [技术细节] Agent调用开始: {}", agentName);

        // ========== 【用户友好进度】发送可理解的进度消息 ==========
        String userFriendlyMessage = getUserFriendlyProgressMessage(agentName, true);

        ExecutionEvent event = new ExecutionEvent();
        event.setMessage(userFriendlyMessage);
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息，不保存到数据库

        sendEvent(ExecutionEventType.AGENT_START, event);
    }

    @Override
    public void afterAgentInvocation(AgentResponse response) {
        String agentName = response.agentName();

        // ========== 【技术细节】只记录到日志 ==========
        log.debug("✅ [技术细节] Agent调用完成: {}", agentName);

        // ========== 【用户友好进度】发送完成消息 ==========
        String userFriendlyMessage = getUserFriendlyProgressMessage(agentName, false);

        ExecutionEvent event = new ExecutionEvent();
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
        event.setMessage("处理过程中遇到问题，请稍后重试...");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.AGENT_ERROR, event);
    }

    @Override
    public void beforeAgentToolExecution(BeforeAgentToolExecution execution) {
        // ========== 【技术细节】只记录到日志 ==========
        log.debug("🔧 [技术细节] 工具执行开始: {}", execution);

        // ========== 【用户友好进度】发送工具执行消息 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setMessage("正在查询数据");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.TOOL_START, event);
    }

    @Override
    public void afterAgentToolExecution(AfterAgentToolExecution execution) {
        // ========== 【技术细节】只记录到日志 ==========
        log.debug("✅ [技术细节] 工具执行完成: {}", execution);

        // ========== 【用户友好进度】发送工具完成消息 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setMessage("数据查询完成");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.TOOL_COMPLETE, event);
    }

    @Override
    public void afterAgenticScopeCreated(AgenticScope scope) {
        // ========== 【技术细节】只记录到日志 ==========
        log.info("🎯 [技术细节] AgenticScope创建: {}", scope.memoryId());

        // 将userId写入AgenticScope，供所有L1子Agent的工具读取
        if (userId != null && !userId.isEmpty()) {
            scope.writeState("userId", userId);
            log.info("🔑 [AgenticScope] 已写入userId: {}", userId);
        }

        // ========== 【用户友好消息】发送任务开始提示 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setMessage("正在为您处理...");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);  // 标记为进度消息

        sendEvent(ExecutionEventType.COMPLETE, event);
    }

    @Override
    public void beforeAgenticScopeDestroyed(AgenticScope scope) {
        // ========== 【技术细节】只记录到日志 ==========
        log.info("🏁 [技术细节] AgenticScope销毁: {}", scope.memoryId());

        // ========== 【用户友好消息】发送完成标记 ==========
        ExecutionEvent event = new ExecutionEvent();
        event.setMessage("处理完成");
        event.setTimestamp(System.currentTimeMillis());
        event.setProgress(true);   // 标记为进度消息
        event.setCompleted(true);  // 标记为完成，前端应隐藏进度指示器

        sendEvent(ExecutionEventType.COMPLETE, event);
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

        if (isStart) {
            switch (simpleName) {
                case "DishRecommendationAgent":
                    return "正在为您搜索菜品";
                case "UserPreferenceAgent":
                    return "正在分析您的偏好";
                case "NutritionGuideAgent":
                    return "正在分析营养成分";
                case "OrderHelperAgent":
                    return "正在处理订单";
                case "MerchantInfoAgent":
                    return "正在查询商家信息";
                case "TimeAwareAgent":
                    return "正在分析时段推荐";
                case "LocationServiceAgent":
                    return "正在查询位置服务";
                case "SupervisorAgent":
                    return "正在为您分析需求";
                default:
                    return "正在处理中";
            }
        } else {
            switch (simpleName) {
                case "DishRecommendationAgent":
                    return "菜品搜索完成";
                case "UserPreferenceAgent":
                    return "偏好分析完成";
                case "NutritionGuideAgent":
                    return "营养分析完成";
                case "OrderHelperAgent":
                    return "订单处理完成";
                case "MerchantInfoAgent":
                    return "商家信息查询完成";
                case "TimeAwareAgent":
                    return "时段分析完成";
                case "LocationServiceAgent":
                    return "位置服务查询完成";
                case "SupervisorAgent":
                    return "需求分析完成";
                default:
                    return "处理完成";
            }
        }
    }

    /**
     * 获取工具执行的用户友好消息
     *
     * @param toolName 工具名称
     * @param isStart 是否为开始阶段
     * @return 用户友好的工具执行描述
     */
    private String getToolExecutionMessage(String toolName, boolean isStart) {
        if (toolName == null) {
            return isStart ? "正在查询数据" : "数据查询完成";
        }

        // 简化工具名称
        String simpleToolName = toolName.replaceAll("Tools$", "");

        if (isStart) {
            return "正在查询数据";
        } else {
            return "数据查询完成";
        }
    }

    @Override
    public boolean inheritedBySubagents() {
        // 监听器继承到子Agent
        return true;
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

        long startTime = System.currentTimeMillis();
        try {
            String eventData = objectMapper.writeValueAsString(event);

            // ========== 【详细日志】发送前记录完整数据 ==========
            log.info("==================== SSE事件发送开始 ====================");
            log.info("📤 [SSE] 事件类型: {}", type.name());
            log.info("📤 [SSE] 事件时间: {}", new java.util.Date());
            log.info("📤 [SSE] 数据长度: {} 字符", eventData.length());
            log.info("📤 [SSE] 完整数据:");
            log.info("─ 开始 ({} 字符) ─", eventData.length());
            log.info(eventData);
            log.info("─ 结束 ─");
            log.info("📤 [SSE] Event对象详情:");
            log.info("   - agentName: {}", event.getAgentName());
            log.info("   - message: {}", event.getMessage());
            log.info("   - isProgress: {}", event.isProgress());
            log.info("   - timestamp: {}", event.getTimestamp());
            log.info("   - toolName: {}", event.getToolName());
            log.info("   - inputs: {}", event.getInputs() != null ? event.getInputs().substring(0, Math.min(100, event.getInputs().length())) + "..." : "null");
            log.info("   - output: {}", event.getOutput() != null ? event.getOutput().substring(0, Math.min(100, event.getOutput().length())) + "..." : "null");
            log.info("=====================================================");

            // ✅ 统一使用 "message" 事件名，前端才能接收
            emitter.send(SseEmitter.event()
                    .name("message")  // 固定使用message事件名
                    .data(eventData));

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ [SSE] 事件发送成功: type={}, 耗时={}ms", type.name(), duration);
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

    /**
     * 格式化输入参数
     */
    private String formatInputs(java.util.Map<String, Object> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(inputs);
        } catch (Exception e) {
            return inputs.toString();
        }
    }

    /**
     * 格式化输出结果
     */
    private String formatOutput(Object output) {
        if (output == null) {
            return "null";
        }
        if (output instanceof String) {
            String str = (String) output;
            return str.length() > 200 ? str.substring(0, 200) + "..." : str;
        }
        // ✅ 避免序列化Agent对象或其他复杂对象
        String outputStr = output.toString();
        return outputStr.length() > 200 ? outputStr.substring(0, 200) + "..." : outputStr;
    }
}
