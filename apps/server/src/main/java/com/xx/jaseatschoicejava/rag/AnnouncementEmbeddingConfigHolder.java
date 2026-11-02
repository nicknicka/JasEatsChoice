package com.xx.jaseatschoicejava.rag;

/**
 * 当前生效的公告 Embedding 配置。
 *
 * baseline 来自环境变量，用作持久化配置缺项时的兜底；current 由 AI 运行时配置在激活/回滚时写入，
 * 检索侧每次调用前读取，因此保存后立即对后续检索生效。
 */
public class AnnouncementEmbeddingConfigHolder {

    private final AnnouncementEmbeddingConfig baseline;
    private volatile AnnouncementEmbeddingConfig current;

    public AnnouncementEmbeddingConfigHolder(AnnouncementRagProperties properties) {
        this(fromProperties(properties));
    }

    AnnouncementEmbeddingConfigHolder(AnnouncementEmbeddingConfig baseline) {
        this.baseline = baseline;
        this.current = baseline;
    }

    public AnnouncementEmbeddingConfig baseline() {
        return baseline;
    }

    public AnnouncementEmbeddingConfig current() {
        return current;
    }

    public void update(AnnouncementEmbeddingConfig config) {
        this.current = config == null ? baseline : config;
    }

    private static AnnouncementEmbeddingConfig fromProperties(AnnouncementRagProperties properties) {
        AnnouncementRagProperties.Embedding embedding = properties.getEmbedding();
        return new AnnouncementEmbeddingConfig(
            embedding.getProvider(), embedding.getModel(), embedding.getDimensions(), embedding.getBaseUrl());
    }
}
