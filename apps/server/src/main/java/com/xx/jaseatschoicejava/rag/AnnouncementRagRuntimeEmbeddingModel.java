package com.xx.jaseatschoicejava.rag;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * 每次调用前重建 Embedding 客户端，保证公告向量化使用轮换后的运行时密钥与当前配置。
 */
public class AnnouncementRagRuntimeEmbeddingModel implements EmbeddingModel {

    private final EmbeddingModelFactory modelFactory;

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
