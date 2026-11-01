package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;

import dev.langchain4j.model.embedding.EmbeddingModel;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.PayloadSchemaType;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

@Testcontainers(disabledWithoutDocker = true)
class QdrantCollectionInitializerTest {

    @Container
    static GenericContainer<?> qdrant = new GenericContainer<>("qdrant/qdrant:v1.17.0")
        .withExposedPorts(6334);

    @Test
    void createsCollectionAndPayloadIndexesIdempotently() throws Exception {
        AnnouncementRagProperties properties = properties("it-" + UUID.randomUUID());
        try (QdrantClient client = client()) {
            QdrantCollectionInitializer initializer = new QdrantCollectionInitializer(client, properties);
            initializer.initialize();
            initializer.initialize();

            var info = client.getCollectionInfoAsync(properties.getQdrant().getCollection()).get();
            assertEquals(4, info.getConfig().getParams().getVectorsConfig().getParams().getSize());
            assertEquals(PayloadSchemaType.Keyword,
                info.getPayloadSchemaOrThrow("sourceType").getDataType());
            assertEquals(PayloadSchemaType.Keyword,
                info.getPayloadSchemaOrThrow("merchantId").getDataType());
            assertEquals(PayloadSchemaType.Keyword,
                info.getPayloadSchemaOrThrow("targetAudience").getDataType());
            assertEquals(PayloadSchemaType.Keyword,
                info.getPayloadSchemaOrThrow("audienceTags").getDataType());
            assertEquals(PayloadSchemaType.Keyword,
                info.getPayloadSchemaOrThrow("status").getDataType());
            assertEquals(PayloadSchemaType.Keyword,
                info.getPayloadSchemaOrThrow("contentId").getDataType());
            assertEquals(PayloadSchemaType.Datetime,
                info.getPayloadSchemaOrThrow("startAt").getDataType());
            assertEquals(PayloadSchemaType.Datetime,
                info.getPayloadSchemaOrThrow("endAt").getDataType());
        }
    }

    @Test
    void normalInfrastructurePath_reportsReadyWithEmbeddingModelAndStore() throws Exception {
        AnnouncementRagProperties properties = properties("it-" + UUID.randomUUID());
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("test-key");
        try (QdrantClient client = client()) {
            AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
                properties, true,
                new AnnouncementRagConfig().announcementEmbeddingModel(properties, zhipuConfig, new AIConfig(), null, new com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry(), java.util.List.of(new ZhipuAnnouncementEmbeddingModelFactory(), new OpenAiCompatibleAnnouncementEmbeddingModelFactory())), client);
            infrastructure.initialize();

            assertEquals(AnnouncementRagState.READY, infrastructure.status().state());
            assertEquals(AnnouncementRagRuntimeEmbeddingModel.class, infrastructure.embeddingModel().getClass());
            assertEquals(QdrantEmbeddingStore.class, infrastructure.embeddingStore().getClass());
        }
    }

    @Test
    void incompatibleDimension_failsFastWithCollectionAndDimensions() throws Exception {
        AnnouncementRagProperties properties = properties("it-" + UUID.randomUUID());
        try (QdrantClient client = client()) {
            client.createCollectionAsync(properties.getQdrant().getCollection(),
                io.qdrant.client.grpc.Collections.VectorParams.newBuilder()
                    .setSize(3)
                    .setDistance(io.qdrant.client.grpc.Collections.Distance.Cosine)
                    .build())
                .get();
            properties.getEmbedding().setDimensions(4);

            QdrantCollectionInitializer initializer = new QdrantCollectionInitializer(client, properties);
            IllegalStateException error = assertThrows(IllegalStateException.class, initializer::initialize);

            assertEquals("公告 RAG collection " + properties.getQdrant().getCollection()
                + " 向量维度为 3，与配置的 embedding 维度 4 不兼容", error.getMessage());
        }
    }

    private static AnnouncementRagProperties properties(String collection) {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        properties.getQdrant().setHost(qdrant.getHost());
        properties.getQdrant().setPort(qdrant.getMappedPort(6334));
        properties.getQdrant().setCollection(collection);
        properties.getEmbedding().setDimensions(4);
        return properties;
    }

    private static QdrantClient client() {
        return new QdrantClient(QdrantGrpcClient.newBuilder(
            qdrant.getHost(), qdrant.getMappedPort(6334), false).build());
    }
}
