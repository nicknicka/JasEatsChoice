package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.service.AnnouncementService;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

/**
 * 真实 Qdrant 上验证：换同维度模型触发重建后，旧向量确实被替换。
 *
 * 判定方式用可观测行为而不是读底层点：旧模型向量在重建后不再命中（余弦相似度为负，
 * 会被 minScore 过滤），新模型向量能命中。若重建没有重写向量，旧模型向量仍会命中。
 */
@Testcontainers(disabledWithoutDocker = true)
class AnnouncementIndexRebuildQdrantTest {

    @Container
    static GenericContainer<?> qdrant = new GenericContainer<>("qdrant/qdrant:v1.17.0")
        .withExposedPorts(6334);

    @Test
    void rebuildReplacesOldVectorsAfterModelSwitch() throws Exception {
        AnnouncementRagProperties properties = properties("rebuild-" + UUID.randomUUID());
        AnnouncementEmbeddingConfigHolder holder = new AnnouncementEmbeddingConfigHolder(properties);
        SwitchableEmbeddingModel model = new SwitchableEmbeddingModel(new float[] {1f, 0f, 0f, 0f});
        AnnouncementEmbeddingModelFactory factory = new SwitchableEmbeddingModelFactory(model);
        AnnouncementService announcementService = mock(AnnouncementService.class);
        Announcement announcement = announcement("a1");
        when(announcementService.getById("a1")).thenReturn(announcement);
        when(announcementService.list(
            any(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.class)))
            .thenReturn(List.of(announcement));

        try (QdrantClient client = client()) {
            AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
                properties, holder::current, () -> model, client);
            RagKnowledgeService knowledgeService =
                new RagKnowledgeService(announcementService, infrastructure);
            AnnouncementIndexRebuilder rebuilder =
                new AnnouncementIndexRebuilder(infrastructure, knowledgeService, Runnable::run);
            AnnouncementRagEmbeddingRuntime runtime = new AnnouncementRagEmbeddingRuntime(
                properties, holder, null, new ZhipuAIConfig(), new AIConfig(),
                List.of(factory), client, rebuilder);

            infrastructure.initialize();
            knowledgeService.syncAnnouncement("a1");

            assertEquals(List.of(1f, 0f, 0f, 0f), storedVector(client, properties),
                "旧模型应写入自己的向量");

            model.setVector(new float[] {-1f, 0f, 0f, 0f});
            runtime.switchConfig(new AnnouncementEmbeddingConfig("stub", "model-b", 4, ""));

            assertEquals(AnnouncementRagState.READY, infrastructure.status().state());
            assertEquals(List.of(-1f, 0f, 0f, 0f), storedVector(client, properties),
                "重建后索引里的向量必须换成新模型的向量");
        }
    }

    /**
     * 直接读回 Qdrant 里的向量：判定「旧向量确实被替换」比走检索路径更直接。
     */
    private List<Float> storedVector(QdrantClient client, AnnouncementRagProperties properties)
            throws Exception {
        String pointId = UUID.nameUUIDFromBytes("announcement:a1:0".getBytes(java.nio.charset.StandardCharsets.UTF_8))
            .toString();
        var points = client.retrieveAsync(
            properties.getQdrant().getCollection(),
            List.of(io.qdrant.client.grpc.Common.PointId.newBuilder().setUuid(pointId).build()),
            io.qdrant.client.grpc.Points.WithPayloadSelector.newBuilder().setEnable(false).build(),
            io.qdrant.client.grpc.Points.WithVectorsSelector.newBuilder().setEnable(true).build(),
            null).get();
        assertEquals(1, points.size(), "索引里应恰好有一个公告分块");
        return points.get(0).getVectors().getVector().getDense().getDataList();
    }

    private Announcement announcement(String id) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setTitle("codex188 临时公告");
        announcement.setContent("索引重建验证内容");
        announcement.setStatus("active");
        announcement.setTargetAudience("all");
        return announcement;
    }

    private static AnnouncementRagProperties properties(String collection) {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        properties.getQdrant().setHost(qdrant.getHost());
        properties.getQdrant().setPort(qdrant.getMappedPort(6334));
        properties.getQdrant().setCollection(collection);
        properties.getEmbedding().setProvider("stub");
        properties.getEmbedding().setModel("model-a");
        properties.getEmbedding().setDimensions(4);
        properties.getEmbedding().setBaseUrl("");
        return properties;
    }

    private static QdrantClient client() {
        return new QdrantClient(QdrantGrpcClient.newBuilder(
            qdrant.getHost(), qdrant.getMappedPort(6334), false).build());
    }

    private static final class SwitchableEmbeddingModelFactory implements AnnouncementEmbeddingModelFactory {

        private final SwitchableEmbeddingModel model;

        private SwitchableEmbeddingModelFactory(SwitchableEmbeddingModel model) {
            this.model = model;
        }

        @Override
        public String providerCode() {
            return "stub";
        }

        @Override
        public boolean isConfigured(String apiKey, String baseUrl) {
            return true;
        }

        @Override
        public EmbeddingModel build(
                String apiKey, String modelName, int dimensions, String baseUrl, java.time.Duration timeout) {
            return model;
        }
    }

    private static final class SwitchableEmbeddingModel implements EmbeddingModel {

        private volatile float[] vector;

        private SwitchableEmbeddingModel(float[] vector) {
            this.vector = vector;
        }

        private void setVector(float[] vector) {
            this.vector = vector;
        }

        @Override
        public Response<Embedding> embed(String text) {
            return Response.from(Embedding.from(vector));
        }

        @Override
        public Response<Embedding> embed(TextSegment textSegment) {
            return embed(textSegment.text());
        }

        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
            return Response.from(textSegments.stream().map(segment -> Embedding.from(vector)).toList());
        }

        @Override
        public int dimension() {
            return vector.length;
        }

        @Override
        public String modelName() {
            return "switchable";
        }
    }
}
