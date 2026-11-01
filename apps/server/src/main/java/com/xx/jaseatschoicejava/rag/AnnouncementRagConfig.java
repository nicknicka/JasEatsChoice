package com.xx.jaseatschoicejava.rag;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        List<AnnouncementEmbeddingModelFactory> embeddingModelFactories) {
        AnnouncementEmbeddingModelFactory embeddingModelFactory = selectEmbeddingModelFactory(
            embeddingModelFactories, properties.getEmbedding().getProvider());
        return new AnnouncementRagRuntimeEmbeddingModel(() ->
            configuredEmbeddingModel(
                properties, zhipuAIConfig, aiConfig, runtimeKeyStore, embeddingModelFactory));
    }

    EmbeddingModel configuredEmbeddingModel(
        AnnouncementRagProperties properties,
        ZhipuAIConfig zhipuAIConfig,
        AIConfig aiConfig,
        AiRuntimeKeyStore runtimeKeyStore,
        AnnouncementEmbeddingModelFactory embeddingModelFactory) {
        String apiKey = AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig, runtimeKeyStore);
        if (apiKey.isBlank()) {
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
        EmbeddingModel announcementEmbeddingModel,
        QdrantClient announcementQdrantClient) {
        return new AnnouncementRagInfrastructure(
            properties, AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig, runtimeKeyStore),
            announcementEmbeddingModel, announcementQdrantClient);
    }
}
