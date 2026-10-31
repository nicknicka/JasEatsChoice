package com.xx.jaseatschoicejava.rag;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.AiApiKeyResolver;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
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
        AIConfig aiConfig) {
        String apiKey = AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig);
        if (apiKey.isBlank()) {
            return new AnnouncementRagDisabledEmbeddingModel();
        }

        String baseUrl = properties.getEmbedding().getBaseUrl();
        return ZhipuAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .model(properties.getEmbedding().getModel())
            .dimensions(properties.getEmbedding().getDimensions())
            .baseUrl(baseUrl == null || baseUrl.isBlank() ? null : baseUrl)
            .connectTimeout(java.time.Duration.ofMillis(zhipuAIConfig.getTimeout()))
            .readTimeout(java.time.Duration.ofMillis(zhipuAIConfig.getTimeout()))
            .build();
    }

    @Bean
    public AnnouncementRagInfrastructure announcementRagInfrastructure(
        AnnouncementRagProperties properties,
        ZhipuAIConfig zhipuAIConfig,
        AIConfig aiConfig,
        EmbeddingModel announcementEmbeddingModel,
        QdrantClient announcementQdrantClient) {
        return new AnnouncementRagInfrastructure(
            properties, AiApiKeyResolver.resolve(zhipuAIConfig, aiConfig),
            announcementEmbeddingModel, announcementQdrantClient);
    }
}
