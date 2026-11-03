package com.xx.jaseatschoicejava.rag;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiEmbeddingRuntimeKeyStore;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

@Configuration
@EnableConfigurationProperties({AnnouncementRagProperties.class, AIConfig.class})
public class AnnouncementRagConfig {

    @Bean(destroyMethod = "close")
    public QdrantClient announcementQdrantClient(AnnouncementRagProperties properties) {
        AnnouncementRagProperties.Qdrant qdrant = properties.getQdrant();
        return new QdrantClient(
            QdrantGrpcClient.newBuilder(qdrant.getHost(), qdrant.getPort(), false).build());
    }

    @Bean
    public AnnouncementEmbeddingConfigHolder announcementEmbeddingConfigHolder(
        AnnouncementRagProperties properties) {
        return new AnnouncementEmbeddingConfigHolder(properties);
    }

    @Bean
    public AnnouncementRagEmbeddingRuntime announcementEmbeddingRuntime(
        AnnouncementRagProperties properties,
        AnnouncementEmbeddingConfigHolder announcementEmbeddingConfigHolder,
        AiEmbeddingRuntimeKeyStore aiEmbeddingRuntimeKeyStore,
        AIConfig aiConfig,
        List<AnnouncementEmbeddingModelFactory> embeddingModelFactories,
        QdrantClient announcementQdrantClient,
        AnnouncementIndexRebuilder announcementIndexRebuilder) {
        return new AnnouncementRagEmbeddingRuntime(
            properties,
            announcementEmbeddingConfigHolder,
            aiEmbeddingRuntimeKeyStore,
            aiConfig,
            embeddingModelFactories,
            announcementQdrantClient,
            announcementIndexRebuilder);
    }

    @Bean
    public AnnouncementIndexRebuilder announcementIndexRebuilder(
        AnnouncementRagInfrastructure announcementRagInfrastructure,
        RagKnowledgeService ragKnowledgeService) {
        return new AnnouncementIndexRebuilder(announcementRagInfrastructure, ragKnowledgeService);
    }

    @Bean
    public EmbeddingModel announcementEmbeddingModel(
        AnnouncementRagEmbeddingRuntime announcementEmbeddingRuntime) {
        return new AnnouncementRagRuntimeEmbeddingModel(announcementEmbeddingRuntime::model);
    }

    @Bean
    public AnnouncementRagInfrastructure announcementRagInfrastructure(
        AnnouncementRagProperties properties,
        AnnouncementEmbeddingConfigHolder announcementEmbeddingConfigHolder,
        AiEmbeddingRuntimeKeyStore aiEmbeddingRuntimeKeyStore,
        AIConfig aiConfig,
        List<AnnouncementEmbeddingModelFactory> embeddingModelFactories,
        ObjectProvider<EmbeddingModel> announcementEmbeddingModel,
        QdrantClient announcementQdrantClient) {
        return new AnnouncementRagInfrastructure(
            properties,
            () -> AnnouncementRagEmbeddingRuntime.usableConfig(
                announcementEmbeddingConfigHolder.current(), aiEmbeddingRuntimeKeyStore,
                aiConfig, embeddingModelFactories),
            announcementEmbeddingModel::getObject,
            announcementQdrantClient);
    }

    static AnnouncementEmbeddingModelFactory selectEmbeddingModelFactory(
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
}
