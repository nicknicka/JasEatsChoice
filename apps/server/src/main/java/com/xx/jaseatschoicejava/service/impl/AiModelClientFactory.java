package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AiModelConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * 根据不可变运行时配置构建模型客户端。
 */
public interface AiModelClientFactory {

    ChatModel buildChatModel(AiModelConfig config);

    ChatModel buildSupervisorModel(AiModelConfig config);

    ChatModel buildVisionModel(AiModelConfig config);

    StreamingChatModel buildStreamingModel(AiModelConfig config);

    AiCapabilitiesDTO capabilities(AiModelConfig config);
}
