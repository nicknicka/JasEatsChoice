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

    public SSEAgentListener(SseEmitter emitter) {
        this.emitter = emitter;
        this.objectMapper = new ObjectMapper();
    }

    public SseEmitter getEmitter() {
        return emitter;
    }

    @Override
    public void beforeAgentInvocation(AgentRequest request) {
        String agentName = request.agentName();
        String inputs = formatInputs(request.inputs());

        log.info("🔧 Agent调用开始: {} | 输入: {}", agentName, inputs);

        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName(agentName);
        event.setAgentId(request.agentId());
        event.setInputs(inputs);
        event.setMessage("正在调用 " + agentName);
        event.setTimestamp(System.currentTimeMillis());

        sendEvent(ExecutionEventType.AGENT_START, event);
    }

    @Override
    public void afterAgentInvocation(AgentResponse response) {
        String agentName = response.agentName();
        String output = formatOutput(response.output());

        log.info("✅ Agent调用完成: {} | 输出: {}", agentName, output);

        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName(agentName);
        event.setAgentId(response.agentId());
        event.setOutput(output);
        event.setMessage("✅ " + agentName + " 执行完成");
        event.setTimestamp(System.currentTimeMillis());

        sendEvent(ExecutionEventType.AGENT_COMPLETE, event);
    }

    @Override
    public void onAgentInvocationError(AgentInvocationError error) {
        String agentName = error.agentName();
        String errorMessage = error.error() != null ? error.error().getMessage() : "Unknown error";

        log.error("❌ Agent调用失败: {} | 错误: {}", agentName, errorMessage);

        ExecutionEvent event = new ExecutionEvent();
        event.setAgentName(agentName);
        event.setAgentId(error.agentId());
        event.setError(errorMessage);
        event.setMessage("❌ " + agentName + " 执行失败: " + errorMessage);
        event.setTimestamp(System.currentTimeMillis());

        sendEvent(ExecutionEventType.AGENT_ERROR, event);
    }

    @Override
    public void beforeAgentToolExecution(BeforeAgentToolExecution execution) {
        // 暂时跳过工具执行监听，API较复杂
        log.debug("工具执行开始: {}", execution);
    }

    @Override
    public void afterAgentToolExecution(AfterAgentToolExecution execution) {
        // 暂时跳过工具执行监听，API较复杂
        log.debug("工具执行完成: {}", execution);
    }

    @Override
    public void afterAgenticScopeCreated(AgenticScope scope) {
        log.info("🎯 AgenticScope创建: {}", scope.memoryId());

        ExecutionEvent event = new ExecutionEvent();
        event.setMessage("🎯 开始处理任务");
        event.setTimestamp(System.currentTimeMillis());

        sendEvent(ExecutionEventType.COMPLETE, event);
    }

    @Override
    public void beforeAgenticScopeDestroyed(AgenticScope scope) {
        log.info("🏁 AgenticScope销毁: {}", scope.memoryId());

        // 发送完成事件
        ExecutionEvent event = new ExecutionEvent();
        event.setMessage("🏁 所有任务已完成");
        event.setTimestamp(System.currentTimeMillis());

        sendEvent(ExecutionEventType.COMPLETE, event);

        // ⚠️ 不在这里关闭SSE连接，由Controller控制关闭时机
        // 如果在这里complete()，会导致后续的FINAL_RESULT无法发送
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
        try {
            String eventData = objectMapper.writeValueAsString(event);
            log.info("📤 [SSE] 发送事件: type={}, data={}", type.name(),
                eventData.length() > 200 ? eventData.substring(0, 200) + "..." : eventData);

            // ✅ 统一使用 "message" 事件名，前端才能接收
            emitter.send(SseEmitter.event()
                    .name("message")  // 固定使用message事件名
                    .data(eventData));

            log.info("✅ [SSE] 事件发送成功: type={}", type.name());
        } catch (IOException e) {
            log.error("❌ [SSE] 发送SSE事件失败: type={}, error={}", type.name(), e.getMessage());
            emitter.completeWithError(e);
        } catch (Exception e) {
            log.error("❌ [SSE] 发送SSE事件异常: type={}, error={}", type.name(), e.getMessage(), e);
            emitter.completeWithError(e);
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
