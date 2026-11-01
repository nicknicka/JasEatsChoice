package com.xx.jaseatschoicejava.service.impl;

import java.util.List;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * AI 供应商适配器：提供已知模型、环境兜底配置、key 解析和按角色构建客户端。
 */
public interface AiModelClientFactory {

    String providerCode();

    String providerDisplayName();

    List<String> knownModels();

    AiModelConfig environmentConfig(AIConfig aiConfig);

    String resolveKey(AiRuntimeKeyStore runtimeKeyStore);

    ChatModel buildChatModel(AiModelConfig config);

    ChatModel buildChatModel(AiModelConfig config, String candidateApiKey);

    ChatModel buildSupervisorModel(AiModelConfig config);

    ChatModel buildSupervisorModel(AiModelConfig config, String candidateApiKey);

    ChatModel buildVisionModel(AiModelConfig config);

    ChatModel buildVisionModel(AiModelConfig config, String candidateApiKey);

    StreamingChatModel buildStreamingModel(AiModelConfig config);

    AiCapabilitiesDTO capabilities(AiModelConfig config);
}
