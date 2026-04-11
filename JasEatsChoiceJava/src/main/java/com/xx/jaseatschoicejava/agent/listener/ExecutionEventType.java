package com.xx.jaseatschoicejava.agent.listener;

/**
 * 执行事件类型
 *
 * 定义Agent执行过程中的各种事件类型
 *
 * @author Claude
 * @since 2026-03-26
 */
public enum ExecutionEventType {
    /**
     * Agent开始执行
     */
    AGENT_START,

    /**
     * Agent执行完成
     */
    AGENT_COMPLETE,

    /**
     * Agent执行错误
     */
    AGENT_ERROR,

    /**
     * 工具开始执行
     */
    TOOL_START,

    /**
     * 工具执行完成
     */
    TOOL_COMPLETE,

    /**
     * 流程初始化
     */
    INIT,

    /**
     * 流程结束
     */
    FINISH,

    /**
     * 全部完成
     */
    COMPLETE
}
