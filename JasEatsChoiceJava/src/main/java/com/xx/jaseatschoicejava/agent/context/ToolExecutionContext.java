package com.xx.jaseatschoicejava.agent.context;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 工具执行上下文管理器
 * 使用 ThreadLocal 存储工具执行信息，用于在流式输出中生成卡片数据
 *
 * @author Claude
 * @since 2026-03-24
 */
public class ToolExecutionContext {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionContext.class);

    /**
     * ThreadLocal 存储当前线程的工具执行栈
     * 栈结构支持多层工具调用
     */
    private static final ThreadLocal<Stack<ToolExecutionInfo>> EXECUTION_STACK = ThreadLocal.withInitial(Stack::new);

    /**
     * 工具执行信息
     */
    @Data
    public static class ToolExecutionInfo {
        private String toolName;           // 工具名称
        private String cardType;           // 对应的卡片类型
        private Map<String, Object> parameters;  // 工具参数
        private Object result;             // 工具执行结果
        private long timestamp;            // 执行时间戳

        public ToolExecutionInfo(String toolName, String cardType, Map<String, Object> parameters) {
            this.toolName = toolName;
            this.cardType = cardType;
            this.parameters = parameters;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * 开始工具执行
     *
     * @param toolName 工具名称
     * @param cardType 卡片类型（如果不需要生成卡片则为null）
     * @param parameters 工具参数
     */
    public static void startExecution(String toolName, String cardType, Map<String, Object> parameters) {
        ToolExecutionInfo info = new ToolExecutionInfo(toolName, cardType, parameters);
        EXECUTION_STACK.get().push(info);
        log.debug("工具执行开始: {}, 卡片类型: {}", toolName, cardType);
    }

    /**
     * 结束工具执行
     *
     * @param result 执行结果
     */
    public static void endExecution(Object result) {
        Stack<ToolExecutionInfo> stack = EXECUTION_STACK.get();
        if (!stack.isEmpty()) {
            ToolExecutionInfo info = stack.pop();
            info.setResult(result);
            log.debug("工具执行结束: {}, 结果类型: {}", info.getToolName(),
                    result != null ? result.getClass().getSimpleName() : "null");
        }
    }

    /**
     * 获取最近的工具执行信息（不弹出栈）
     *
     * @return 最近的工具执行信息，如果没有则返回null
     */
    public static ToolExecutionInfo peekLastExecution() {
        Stack<ToolExecutionInfo> stack = EXECUTION_STACK.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * 获取所有需要生成卡片的工具执行信息
     *
     * @return 卡片类型和执行信息的映射
     */
    public static Map<String, ToolExecutionInfo> getCardExecutions() {
        Map<String, ToolExecutionInfo> cardExecutions = new HashMap<>();
        Stack<ToolExecutionInfo> stack = EXECUTION_STACK.get();

        for (ToolExecutionInfo info : stack) {
            if (info.getCardType() != null && !info.getCardType().isEmpty()) {
                cardExecutions.put(info.getCardType(), info);
            }
        }

        return cardExecutions;
    }

    /**
     * 清空当前线程的工具执行栈
     * 通常在请求处理完成后调用
     */
    public static void clear() {
        EXECUTION_STACK.get().clear();
        log.debug("工具执行栈已清空");
    }

    /**
     * 获取当前执行栈的大小
     */
    public static int getStackSize() {
        return EXECUTION_STACK.get().size();
    }
}
