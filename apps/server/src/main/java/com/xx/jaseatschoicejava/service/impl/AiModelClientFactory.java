package com.xx.jaseatschoicejava.service.impl;

import java.util.List;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.dto.AiModelCandidateDTO;
import com.xx.jaseatschoicejava.dto.AiModelOption;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * AI 供应商适配器：提供已知模型、环境兜底配置、key 解析和按角色构建客户端。
 */
public interface AiModelClientFactory {

    String providerCode();

    String providerDisplayName();

    List<String> knownModels();

    /**
     * 返回 provider 当前可用的模型列表。默认回落到静态预设；能实时发现的适配器
     * （如 openai-compatible 调 {baseUrl}/models）覆盖此方法。失败时返回空列表，调用方回落预设。
     */
    default List<AiModelOption> discoverModels(AiModelCandidateDTO candidate) {
        return knownModels().stream()
            .map(id -> AiModelOption.builder().id(id).name(id).build())
            .toList();
    }

    AiModelConfig environmentConfig(AIConfig aiConfig);

    String resolveKey(AiRuntimeKeyStore runtimeKeyStore);

    ChatModel buildChatModel(AiModelConfig config);

    ChatModel buildChatModel(AiModelConfig config, String candidateApiKey);

    ChatModel buildSupervisorModel(AiModelConfig config);

    ChatModel buildSupervisorModel(AiModelConfig config, String candidateApiKey);

    ChatModel buildVisionModel(AiModelConfig config);

    ChatModel buildVisionModel(AiModelConfig config, String candidateApiKey);

    StreamingChatModel buildStreamingModel(AiModelConfig config);

    StreamingChatModel buildStreamingModel(AiModelConfig config, String candidateApiKey);

    AiCapabilitiesDTO capabilities(AiModelConfig config);
}
