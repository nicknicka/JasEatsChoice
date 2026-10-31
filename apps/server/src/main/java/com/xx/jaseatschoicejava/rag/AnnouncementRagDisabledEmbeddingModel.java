package com.xx.jaseatschoicejava.rag;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

public class AnnouncementRagDisabledEmbeddingModel implements EmbeddingModel {

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        throw new IllegalStateException("公告 RAG Embedding 未配置");
    }
}
