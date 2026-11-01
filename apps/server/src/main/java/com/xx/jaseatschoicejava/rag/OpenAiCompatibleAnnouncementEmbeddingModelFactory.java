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
