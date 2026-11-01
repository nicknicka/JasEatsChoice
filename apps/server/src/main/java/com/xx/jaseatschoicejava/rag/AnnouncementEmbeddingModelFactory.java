package com.xx.jaseatschoicejava.rag;

import java.time.Duration;

import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * 公告 RAG 的 Embedding 客户端工厂。每个实现对应一个 provider，
 * 使公告索引与检索不依赖具体供应方。
 */
public interface AnnouncementEmbeddingModelFactory {

    /**
     * provider 代码，与配置中的取值对应。
     */
    String providerCode();

    /**
     * 按给定参数构建 Embedding 客户端。
     */
    EmbeddingModel build(String apiKey, String model, int dimensions, String baseUrl, Duration timeout);

    /**
     * 配置是否完整到可以构建客户端。
     *
     * 默认要求有 apiKey；本地 OpenAI 兼容端点（如 Ollama）不需要 key，可覆盖为只校验地址。
     */
    default boolean isConfigured(String apiKey, String baseUrl) {
        return apiKey != null && !apiKey.isBlank();
    }
}
