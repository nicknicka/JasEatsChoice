package com.xx.jaseatschoicejava.rag;

import java.time.Duration;

import org.springframework.stereotype.Component;

import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * 智谱 Embedding provider 实现。
 */
@Component
public class ZhipuAnnouncementEmbeddingModelFactory implements AnnouncementEmbeddingModelFactory {

    @Override
    public String providerCode() {
        return "zhipu";
    }

    @Override
    public EmbeddingModel build(String apiKey, String model, int dimensions, String baseUrl, Duration timeout) {
        return ZhipuAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .model(model)
            .dimensions(dimensions)
            .baseUrl(baseUrl == null || baseUrl.isBlank() ? null : baseUrl)
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .build();
    }
}
