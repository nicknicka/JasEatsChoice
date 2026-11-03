package com.xx.jaseatschoicejava.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 模型能力清单。
 *
 * 前端根据此清单动态调整 UI：不支持的功能隐藏入口或显示降级提示。
 * 由 /v1/ai/capabilities 端点返回。
 *
 * @since 2026-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCapabilitiesDTO {

    /**
     * 当前 AI 供应商是否可用（API key 是否已配置）。
     * false 时所有能力字段均为 false，前端应整体降级。
     */
    private boolean available;

    /**
     * 当前适配器代码，例如 openai-compatible
     */
    private String provider;

    /**
     * 文本模型名
     */
    private String model;

    /**
     * 视觉模型名（可能与文本模型相同）
     */
    private String visionModel;

    /**
     * 是否支持文本对话
     */
    private boolean textChat;

    /**
     * 是否支持流式输出（SSE / WebSocket 逐 token）
     */
    private boolean streaming;

    /**
     * 是否支持视觉理解（图片识别）
     */
    private boolean vision;

    /**
     * 是否支持函数调用 / Tool Calling
     */
    private boolean functionCalling;

    /**
     * 是否支持多轮 Agent（监督代理 + 工具调度）
     */
    private boolean agentic;

    /**
     * 不可用时的提示文案，供前端直接展示
     */
    private String unavailableMessage;
}
