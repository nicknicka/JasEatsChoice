package com.xx.jaseatschoicejava.rag;

import java.util.List;
import java.util.function.Supplier;

import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * 每次调用前重新解析 active key，保证公告 Embedding 也使用轮换后的运行时密钥。
 */
public class AnnouncementRagRuntimeEmbeddingModel implements EmbeddingModel {

    private final EmbeddingModelFactory modelFactory;

    public AnnouncementRagRuntimeEmbeddingModel(
            AnnouncementRagProperties properties,
            ZhipuAIConfig zhipuAIConfig,
            AIConfig aiConfig,
            AiRuntimeKeyStore runtimeKeyStore) {
        this.modelFactory = () -> new AnnouncementRagConfig().configuredEmbeddingModel(
            properties, zhipuAIConfig, aiConfig, runtimeKeyStore);
    }

    AnnouncementRagRuntimeEmbeddingModel(EmbeddingModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    @Override
    public Response<Embedding> embed(String text) {
        return modelFactory.create().embed(text);
    }

    @Override
    public Response<Embedding> embed(TextSegment textSegment) {
        return modelFactory.create().embed(textSegment);
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        return modelFactory.create().embedAll(textSegments);
    }

    @Override
    public int dimension() {
        return modelFactory.create().dimension();
    }

    @Override
    public String modelName() {
        return modelFactory.create().modelName();
    }

    @FunctionalInterface
    interface EmbeddingModelFactory {
        EmbeddingModel create();
    }
}
