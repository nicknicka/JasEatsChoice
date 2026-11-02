package com.xx.jaseatschoicejava.rag;

import java.util.Objects;
import java.util.function.Supplier;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import jakarta.annotation.PostConstruct;

public class AnnouncementRagInfrastructure {

    private final AnnouncementRagProperties properties;
    private final Supplier<AnnouncementEmbeddingConfig> embeddingConfig;
    private final EmbeddingModel embeddingModel;
    private final QdrantClient qdrantClient;
    private EmbeddingStore<TextSegment> embeddingStore;
    private AnnouncementEmbeddingConfig initializedWith;
    private AnnouncementRagStatus status = new AnnouncementRagStatus(
        AnnouncementRagState.DISABLED, "初始化未执行");

    public AnnouncementRagInfrastructure(
        AnnouncementRagProperties properties,
        Supplier<AnnouncementEmbeddingConfig> embeddingConfig,
        EmbeddingModel embeddingModel,
        QdrantClient qdrantClient) {
        this.properties = properties;
        this.embeddingConfig = embeddingConfig;
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
        AnnouncementEmbeddingConfig config = currentEmbeddingConfig();
        if (config == null) {
            embeddingStore = null;
            initializedWith = null;
            status = new AnnouncementRagStatus(AnnouncementRagState.NOT_CONFIGURED, "公告 RAG Embedding 未配置");
            return;
        }
        if (qdrantClient == null) {
            status = new AnnouncementRagStatus(AnnouncementRagState.UNAVAILABLE, "Qdrant 客户端未初始化");
            return;
        }
        try {
            new QdrantCollectionInitializer(qdrantClient, properties).initialize(config.dimensions());
            embeddingStore = new QdrantEmbeddingStore(
                qdrantClient, properties.getQdrant().getCollection(), "text");
            initializedWith = config;
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

    /**
     * 检索侧每次调用前解析。运行中切换 Embedding 配置后按新维度重建 store，
     * 因此保存后后续检索立即使用新端点，不需要重启后端。
     */
    public EmbeddingStore<TextSegment> embeddingStore() {
        ensureInitialized();
        return embeddingStore;
    }

    private synchronized void ensureInitialized() {
        AnnouncementEmbeddingConfig config = currentEmbeddingConfig();
        if (embeddingStore != null && Objects.equals(initializedWith, config)) {
            return;
        }
        initialize();
    }

    private AnnouncementEmbeddingConfig currentEmbeddingConfig() {
        return embeddingConfig == null ? null : embeddingConfig.get();
    }
}
