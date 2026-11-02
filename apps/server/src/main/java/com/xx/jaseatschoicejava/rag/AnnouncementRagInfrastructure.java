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
    private final Supplier<EmbeddingModel> embeddingModel;
    private final QdrantClient qdrantClient;
    private volatile EmbeddingStore<TextSegment> embeddingStore;
    private AnnouncementEmbeddingConfig initializedWith;
    private volatile AnnouncementRagStatus status = new AnnouncementRagStatus(
        AnnouncementRagState.DISABLED, "初始化未执行");

    public AnnouncementRagInfrastructure(
        AnnouncementRagProperties properties,
        Supplier<AnnouncementEmbeddingConfig> embeddingConfig,
        Supplier<EmbeddingModel> embeddingModel,
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
            updateStatus(AnnouncementRagState.DISABLED, "公告 RAG 已显式关闭");
            return;
        }
        AnnouncementEmbeddingConfig config = currentEmbeddingConfig();
        if (config == null) {
            embeddingStore = null;
            initializedWith = null;
            updateStatus(AnnouncementRagState.NOT_CONFIGURED, "公告 RAG Embedding 未配置");
            return;
        }
        if (qdrantClient == null) {
            updateStatus(AnnouncementRagState.UNAVAILABLE, "Qdrant 客户端未初始化");
            return;
        }
        try {
            new QdrantCollectionInitializer(qdrantClient, properties).initialize(config.dimensions());
            embeddingStore = new QdrantEmbeddingStore(
                qdrantClient, properties.getQdrant().getCollection(), "text");
            initializedWith = config;
            updateStatus(AnnouncementRagState.READY, "公告 RAG 初始化完成");
        }
        catch (IllegalStateException exception) {
            embeddingStore = null;
            updateStatus(AnnouncementRagState.UNAVAILABLE, exception.getMessage());
            throw exception;
        }
        catch (Exception exception) {
            embeddingStore = null;
            updateStatus(AnnouncementRagState.UNAVAILABLE, "Qdrant 不可用: " + exception.getMessage());
        }
    }

    /**
     * 重建流程接管状态期间，初始化成功不得把它改回就绪——否则重建中的检索窗口会被重新打开。
     */
    private void updateStatus(AnnouncementRagState state, String reason) {
        if (state == AnnouncementRagState.READY && status.state() == AnnouncementRagState.REBUILDING) {
            return;
        }
        status = new AnnouncementRagStatus(state, reason);
    }

    public AnnouncementRagStatus status() {
        return status;
    }

    /**
     * 进入「重建中」：公告检索失败关闭，不返回可能基于旧向量的结果。
     */
    public synchronized void markRebuilding(String reason) {
        status = new AnnouncementRagStatus(AnnouncementRagState.REBUILDING, reason);
    }

    public synchronized void markReady(String reason) {
        status = new AnnouncementRagStatus(AnnouncementRagState.READY, reason);
    }

    public synchronized void markUnavailable(String reason) {
        status = new AnnouncementRagStatus(AnnouncementRagState.UNAVAILABLE, reason);
    }

    /**
     * 检索/索引侧每次调用前解析 Embedding 客户端；惰性解析同时避免与运行时配置互相依赖成环。
     */
    public EmbeddingModel embeddingModel() {
        return embeddingModel == null ? null : embeddingModel.get();
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
