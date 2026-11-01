package com.xx.jaseatschoicejava.rag;

import java.time.Duration;

import org.springframework.stereotype.Component;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

/**
 * OpenAI 兼容 Embedding provider 实现，适用于本地 embedding 服务与兼容网关。
 */
@Component
public class OpenAiCompatibleAnnouncementEmbeddingModelFactory implements AnnouncementEmbeddingModelFactory {

    @Override
    public String providerCode() {
        return "openai-compatible";
    }

    /**
     * OpenAI 兼容端点只要有服务地址即可：本地 Ollama 之类不校验 Authorization，
     * 远端网关需要的 key 仍可照常通过 AI_API_KEY / 运行时 key 提供。
     */
    @Override
    public boolean isConfigured(String apiKey, String baseUrl) {
        return baseUrl != null && !baseUrl.isBlank();
    }

    @Override
    public EmbeddingModel build(String apiKey, String model, int dimensions, String baseUrl, Duration timeout) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException(
                "openai-compatible Embedding 需要配置服务地址 AI_EMBEDDING_BASE_URL");
        }

        OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder builder = OpenAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .modelName(model)
            .timeout(timeout);
        if (dimensions > 0) {
            builder.dimensions(dimensions);
        }
        return builder.build();
    }
}
