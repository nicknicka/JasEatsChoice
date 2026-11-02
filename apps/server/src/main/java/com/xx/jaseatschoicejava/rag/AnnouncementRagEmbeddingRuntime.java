package com.xx.jaseatschoicejava.rag;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.AiEmbeddingRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.service.AnnouncementEmbeddingRuntime;
import com.xx.jaseatschoicejava.util.SecretFingerprint;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.qdrant.client.QdrantClient;

/**
 * 公告 Embedding 运行时实现。
 *
 * 配置与密钥都按调用时解析：换地址/密钥后，检索与索引下一次调用就用新端点，不需要重启；
 * 换模型会改变向量空间，激活后自动触发全量重建索引（重建期间公告检索失败关闭）；
 * 维度与现有 collection 不一致由 {@link #validate} 拒绝，需要新建 collection。
 */
public class AnnouncementRagEmbeddingRuntime implements AnnouncementEmbeddingRuntime {

    private static final int DEFAULT_TIMEOUT_MS = 30000;

    private final AnnouncementRagProperties properties;
    private final AnnouncementEmbeddingConfigHolder holder;
    private final AiEmbeddingRuntimeKeyStore embeddingKeyStore;
    private final ZhipuAIConfig zhipuAIConfig;
    private final AIConfig aiConfig;
    private final List<AnnouncementEmbeddingModelFactory> factories;
    private final QdrantClient qdrantClient;
    private final AnnouncementIndexRebuilder indexRebuilder;

    public AnnouncementRagEmbeddingRuntime(
            AnnouncementRagProperties properties,
            AnnouncementEmbeddingConfigHolder holder,
            AiEmbeddingRuntimeKeyStore embeddingKeyStore,
            ZhipuAIConfig zhipuAIConfig,
            AIConfig aiConfig,
            List<AnnouncementEmbeddingModelFactory> factories,
            QdrantClient qdrantClient,
            AnnouncementIndexRebuilder indexRebuilder) {
        this.properties = properties;
        this.holder = holder;
        this.embeddingKeyStore = embeddingKeyStore;
        this.zhipuAIConfig = zhipuAIConfig;
        this.aiConfig = aiConfig;
        this.factories = factories;
        this.qdrantClient = qdrantClient;
        this.indexRebuilder = indexRebuilder;
    }

    /**
     * 某份配置在当前密钥环境下是否可用；可用时返回该配置，否则返回 null。
     *
     * 供基础设施按调用判断「是否已配置」，避免基础设施反向依赖运行时实例。
     */
    static AnnouncementEmbeddingConfig usableConfig(
            AnnouncementEmbeddingConfig config,
            AiEmbeddingRuntimeKeyStore embeddingKeyStore,
            ZhipuAIConfig zhipuAIConfig,
            AIConfig aiConfig,
            List<AnnouncementEmbeddingModelFactory> factories) {
        if (config == null) {
            return null;
        }
        AnnouncementEmbeddingModelFactory factory =
            AnnouncementRagConfig.selectEmbeddingModelFactory(factories, config.provider());
        String apiKey = AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig,
            embeddingKeyStore == null ? "" : embeddingKeyStore.readKey());
        return factory.isConfigured(apiKey, config.baseUrl()) ? config : null;
    }

    /**
     * 当前生效配置对应的 Embedding 客户端；未配置完整时返回占位实现，检索按降级处理。
     */
    public EmbeddingModel model() {
        AnnouncementEmbeddingConfig config = holder.current();
        AnnouncementEmbeddingModelFactory factory = factory(config.provider());
        String apiKey = apiKey(null);
        if (!factory.isConfigured(apiKey, config.baseUrl())) {
            return new AnnouncementRagDisabledEmbeddingModel();
        }
        return factory.build(apiKey, config.model(), config.dimensions(), config.baseUrl(), timeout());
    }

    /**
     * 当前生效配置；缺少服务地址或密钥时返回 null。
     */
    public AnnouncementEmbeddingConfig usable() {
        return usableConfig(holder.current(), embeddingKeyStore, zhipuAIConfig, aiConfig, factories);
    }

    @Override
    public AnnouncementEmbeddingConfig effective(AnnouncementEmbeddingConfig persisted) {
        AnnouncementEmbeddingConfig fallback = holder.baseline();
        if (persisted == null) {
            return fallback;
        }
        return new AnnouncementEmbeddingConfig(
            hasText(persisted.provider()) ? persisted.provider() : fallback.provider(),
            hasText(persisted.model()) ? persisted.model() : fallback.model(),
            persisted.dimensions() > 0 ? persisted.dimensions() : fallback.dimensions(),
            persisted.baseUrl() != null ? persisted.baseUrl() : fallback.baseUrl());
    }

    @Override
    public void validate(AnnouncementEmbeddingConfig candidate, String candidateKey) {
        AnnouncementEmbeddingModelFactory factory = factory(candidate.provider());
        String apiKey = apiKey(candidateKey);
        if (!factory.isConfigured(apiKey, candidate.baseUrl())) {
            throw new BusinessException("EMBEDDING_CONFIG_INCOMPLETE",
                "公告 Embedding 配置不完整：该 provider 需要服务地址或密钥");
        }

        int actualDimensions = measuredDimensions(factory, candidate, apiKey);
        if (candidate.dimensions() > 0 && candidate.dimensions() != actualDimensions) {
            throw new BusinessException("EMBEDDING_DIMENSION_CONFIG_MISMATCH",
                "配置的向量维度 " + candidate.dimensions() + " 与实际返回的 " + actualDimensions + " 不一致");
        }
        Integer collectionDimensions = collectionDimensions();
        if (collectionDimensions != null && collectionDimensions != actualDimensions) {
            throw new BusinessException("EMBEDDING_DIMENSION_MISMATCH",
                "实测向量维度 " + actualDimensions + " 与现有向量库维度 " + collectionDimensions
                    + " 不一致，需要新建 collection");
        }
    }

    /**
     * 启动/重启恢复：索引本来就是按这份配置建的，因此只设为当前生效值，不触发重建。
     */
    @Override
    public void activate(AnnouncementEmbeddingConfig config) {
        holder.update(config);
    }

    /**
     * 管理员切换或回滚配置：换模型（或维度）会改变向量空间，旧向量对新模型无意义，
     * 因此生效后立刻触发全量重建——重建期间公告检索失败关闭，绝不拿旧向量凑答案。
     *
     * @return 是否因此触发了索引重建
     */
    @Override
    public boolean switchConfig(AnnouncementEmbeddingConfig config) {
        AnnouncementEmbeddingConfig previous = holder.current();
        boolean vectorSpaceChanged = !Objects.equals(previous.model(), config.model())
            || previous.dimensions() != config.dimensions();
        holder.update(config);
        if (vectorSpaceChanged && indexRebuilder != null) {
            indexRebuilder.requestRebuild();
            return true;
        }
        return false;
    }

    @Override
    public AnnouncementRagStatus status() {
        return indexRebuilder == null ? null : indexRebuilder.status();
    }

    @Override
    public void rebuildIndex() {
        if (indexRebuilder != null) {
            indexRebuilder.requestRebuild();
        }
    }

    @Override
    public List<String> providers() {
        return factories == null ? List.of()
            : factories.stream().map(AnnouncementEmbeddingModelFactory::providerCode).toList();
    }

    @Override
    public KeyStatus keyStatus() {
        String apiKey = apiKey(null);
        if (!hasText(apiKey)) {
            return new KeyStatus(false, null, null);
        }
        String updatedAt = embeddingKeyStore == null ? null : embeddingKeyStore.updatedAt();
        return new KeyStatus(true, SecretFingerprint.shortSha256(apiKey), updatedAt);
    }

    private int measuredDimensions(
            AnnouncementEmbeddingModelFactory factory,
            AnnouncementEmbeddingConfig candidate,
            String apiKey) {
        EmbeddingModel model = factory.build(
            apiKey, candidate.model(), candidate.dimensions(), candidate.baseUrl(), timeout());
        try {
            return model.embed("ping").content().dimension();
        }
        catch (Exception exception) {
            throw new BusinessException("EMBEDDING_CONNECTIVITY_FAILED",
                "公告 Embedding 连通性校验失败：" + safeMessage(exception, apiKey));
        }
    }

    private Integer collectionDimensions() {
        if (qdrantClient == null) {
            return null;
        }
        try {
            return new QdrantCollectionInitializer(qdrantClient, properties).currentDimension();
        }
        catch (Exception exception) {
            throw new BusinessException("EMBEDDING_COLLECTION_UNAVAILABLE",
                "读取向量库 collection 维度失败：" + exception.getMessage());
        }
    }

    /**
     * 候选 key 优先，其次 Embedding 独立的运行时密钥槽位，最后环境变量兜底。
     * 对话模型的 active key 不参与 Embedding 解析。
     */
    private String apiKey(String candidateKey) {
        String runtimeKey = candidateKey;
        if (!hasText(runtimeKey)) {
            runtimeKey = embeddingKeyStore == null ? "" : embeddingKeyStore.readKey();
        }
        return AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig, runtimeKey);
    }

    private AnnouncementEmbeddingModelFactory factory(String provider) {
        return AnnouncementRagConfig.selectEmbeddingModelFactory(factories, provider);
    }

    private Duration timeout() {
        Integer millis = zhipuAIConfig == null ? null : zhipuAIConfig.getTimeout();
        return Duration.ofMillis(millis == null || millis <= 0 ? DEFAULT_TIMEOUT_MS : millis);
    }

    private String safeMessage(Exception exception, String apiKey) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "连接失败";
        }
        return hasText(apiKey) ? message.replace(apiKey, "***") : message;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
