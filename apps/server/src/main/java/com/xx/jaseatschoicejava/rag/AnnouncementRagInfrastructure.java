package com.xx.jaseatschoicejava.rag;

import java.util.concurrent.TimeUnit;

import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import jakarta.annotation.PostConstruct;

public class AnnouncementRagInfrastructure {

    private final AnnouncementRagProperties properties;
    private final ZhipuAIConfig zhipuAIConfig;
    private final EmbeddingModel embeddingModel;
    private final QdrantClient qdrantClient;
    private EmbeddingStore<TextSegment> embeddingStore;
    private AnnouncementRagStatus status = new AnnouncementRagStatus(
        AnnouncementRagState.DISABLED, "初始化未执行");

    public AnnouncementRagInfrastructure(
        AnnouncementRagProperties properties,
        ZhipuAIConfig zhipuAIConfig,
        EmbeddingModel embeddingModel,
        QdrantClient qdrantClient) {
        this.properties = properties;
        this.zhipuAIConfig = zhipuAIConfig;
        this.embeddingModel = embeddingModel;
        this.qdrantClient = qdrantClient;
    }

    @PostConstruct
    public void initializeOnStartup() {
        initialize();
    }

    public synchronized void initialize() {
        if (!properties.isEnabled()) {
            status = new AnnouncementRagStatus(AnnouncementRagState.DISABLED, "公告 RAG 已显式关闭");
            return;
        }
        if (zhipuAIConfig.getApiKey() == null || zhipuAIConfig.getApiKey().isBlank()) {
            status = new AnnouncementRagStatus(AnnouncementRagState.NOT_CONFIGURED, "智谱 API key 未配置");
            return;
        }
        if (qdrantClient == null) {
            status = new AnnouncementRagStatus(AnnouncementRagState.UNAVAILABLE, "Qdrant 客户端未初始化");
            return;
        }
        try {
            new QdrantCollectionInitializer(qdrantClient, properties).initialize();
            embeddingStore = new QdrantEmbeddingStore(
                qdrantClient, properties.getQdrant().getCollection(), "text");
            status = new AnnouncementRagStatus(AnnouncementRagState.READY, "公告 RAG 初始化完成");
        }
        catch (IllegalStateException exception) {
            embeddingStore = null;
            status = new AnnouncementRagStatus(AnnouncementRagState.UNAVAILABLE, exception.getMessage());
            throw exception;
        }
        catch (Exception exception) {
            embeddingStore = null;
            status = new AnnouncementRagStatus(AnnouncementRagState.UNAVAILABLE,
                "Qdrant 不可用: " + exception.getMessage());
        }
    }

    public AnnouncementRagStatus status() {
        return status;
    }

    public EmbeddingModel embeddingModel() {
        return embeddingModel;
    }

    public EmbeddingStore<TextSegment> embeddingStore() {
        return embeddingStore;
    }
}
