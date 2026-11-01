package com.xx.jaseatschoicejava.rag;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

@Configuration
@EnableConfigurationProperties({AnnouncementRagProperties.class, ZhipuAIConfig.class, AIConfig.class})
public class AnnouncementRagConfig {

    @Bean(destroyMethod = "close")
    public QdrantClient announcementQdrantClient(AnnouncementRagProperties properties) {
        AnnouncementRagProperties.Qdrant qdrant = properties.getQdrant();
        return new QdrantClient(
            QdrantGrpcClient.newBuilder(qdrant.getHost(), qdrant.getPort(), false).build());
    }

    @Bean
    public EmbeddingModel announcementEmbeddingModel(
        AnnouncementRagProperties properties,
        ZhipuAIConfig zhipuAIConfig,
        AIConfig aiConfig,
        AiRuntimeKeyStore runtimeKeyStore,
        DynamicAiModelRegistry dynamicAiModelRegistry,
        List<AnnouncementEmbeddingModelFactory> embeddingModelFactories) {
        AnnouncementEmbeddingModelFactory embeddingModelFactory = selectEmbeddingModelFactory(
            embeddingModelFactories, properties.getEmbedding().getProvider());
        return new AnnouncementRagRuntimeEmbeddingModel(() ->
            configuredEmbeddingModel(
                properties, zhipuAIConfig, aiConfig, runtimeKeyStore,
                activeProvider(dynamicAiModelRegistry, aiConfig), embeddingModelFactory));
    }

    EmbeddingModel configuredEmbeddingModel(
        AnnouncementRagProperties properties,
        ZhipuAIConfig zhipuAIConfig,
        AIConfig aiConfig,
        AiRuntimeKeyStore runtimeKeyStore,
        String activeProvider,
        AnnouncementEmbeddingModelFactory embeddingModelFactory) {
        String apiKey = embeddingApiKey(properties, zhipuAIConfig, aiConfig, runtimeKeyStore, activeProvider);
        if (!embeddingModelFactory.isConfigured(apiKey, properties.getEmbedding().getBaseUrl())) {
            return new AnnouncementRagDisabledEmbeddingModel();
        }

        AnnouncementRagProperties.Embedding embedding = properties.getEmbedding();
        return embeddingModelFactory.build(
            apiKey,
            embedding.getModel(),
            embedding.getDimensions(),
            embedding.getBaseUrl(),
            Duration.ofMillis(zhipuAIConfig.getTimeout()));
    }

    AnnouncementEmbeddingModelFactory selectEmbeddingModelFactory(
        List<AnnouncementEmbeddingModelFactory> embeddingModelFactories, String provider) {
        if (embeddingModelFactories == null || embeddingModelFactories.isEmpty()) {
            throw new IllegalStateException("没有可用的 Embedding provider 实现");
        }
        for (AnnouncementEmbeddingModelFactory factory : embeddingModelFactories) {
            if (factory.providerCode().equals(provider)) {
                return factory;
            }
        }
        List<String> available = embeddingModelFactories.stream()
            .map(AnnouncementEmbeddingModelFactory::providerCode)
            .toList();
        throw new IllegalArgumentException(
            "不支持的 Embedding provider：" + provider + "，可用：" + available);
    }

    @Bean
    public AnnouncementRagInfrastructure announcementRagInfrastructure(
        AnnouncementRagProperties properties,
        ZhipuAIConfig zhipuAIConfig,
        AIConfig aiConfig,
        AiRuntimeKeyStore runtimeKeyStore,
        DynamicAiModelRegistry dynamicAiModelRegistry,
        List<AnnouncementEmbeddingModelFactory> embeddingModelFactories,
        EmbeddingModel announcementEmbeddingModel,
        QdrantClient announcementQdrantClient) {
        AnnouncementEmbeddingModelFactory embeddingModelFactory = selectEmbeddingModelFactory(
            embeddingModelFactories, properties.getEmbedding().getProvider());
        String apiKey = embeddingApiKey(properties, zhipuAIConfig, aiConfig, runtimeKeyStore,
            activeProvider(dynamicAiModelRegistry, aiConfig));
        return new AnnouncementRagInfrastructure(
            properties,
            embeddingModelFactory.isConfigured(apiKey, properties.getEmbedding().getBaseUrl()),
            announcementEmbeddingModel, announcementQdrantClient);
    }

    /**
     * 公告向量化使用的 key。
     *
     * 管理员轮换的运行时 key 属于“当前激活的对话供应商”，只有 embedding 供应商与它一致时才能复用；
     * 否则会把对话供应商的 key 发到 embedding 供应商（例如拿第三方对话 key 调智谱 embedding）。
     */
    String embeddingApiKey(
            AnnouncementRagProperties properties,
            ZhipuAIConfig zhipuAIConfig,
            AIConfig aiConfig,
            AiRuntimeKeyStore runtimeKeyStore,
            String activeProvider) {
        String embeddingProvider = properties.getEmbedding().getProvider();
        if (activeProvider != null && !activeProvider.isBlank()
                && activeProvider.equals(embeddingProvider)) {
            return AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig, runtimeKeyStore);
        }
        return AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig);
    }

    private String activeProvider(DynamicAiModelRegistry registry, AIConfig aiConfig) {
        if (registry != null && registry.current() != null && registry.current().config() != null) {
            return registry.current().config().getProvider();
        }
        return aiConfig == null ? null : aiConfig.getProvider();
    }
}
