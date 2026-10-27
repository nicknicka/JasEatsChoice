package com.xx.jaseatschoicejava.agent.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;

/**
 * AI 模型供应商抽象。
 *
 * 业务代码（Agent / Service）只依赖 LangChain4j 标准接口，不直接引用任何
 * 供应商 SDK。具体供应商通过 {@code ai.provider} 配置切换，
 * 由对应的 {@code @ConditionalOnProperty} 配置类产出本接口的实现。
 *
 * 当 {@link #isAvailable()} 返回 false（API key 未配置等）时，
 * {@link LangChain4jConfig} 会降级为 Noop 实现，保证 Spring 上下文能正常启动。
 *
 * @since 2026-10-27
 */
public interface ChatModelProvider {

    /**
     * 供应商是否可用（通常取决于 API key 是否配置）。
     */
    boolean isAvailable();

    /**
     * 构建通用文本对话模型。
     */
    ChatModel buildAiModel();

    /**
     * 构建 Supervisor 专用模型（推理能力更强、温度更低）。
     */
    ChatModel buildSupervisorModel();

    /**
     * 构建视觉识别模型。
     */
    ChatModel buildVisionModel();

    /**
     * 构建流式对话模型。
     */
    StreamingChatModel buildStreamingModel();

    /**
     * 返回当前供应商的能力清单。
     * 用于前端动态调整 UI 和降级提示。
     */
    AiCapabilitiesDTO getCapabilities();
}
