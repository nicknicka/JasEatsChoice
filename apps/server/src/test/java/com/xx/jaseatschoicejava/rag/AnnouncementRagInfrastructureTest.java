package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.ServerSocket;
import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

class AnnouncementRagInfrastructureTest {

    private static AnnouncementEmbeddingConfig embeddingConfig() {
        return new AnnouncementEmbeddingConfig("zhipu", "embedding-3", 2048, "");
    }

    @Test
    void missingEmbeddingKey_reportsNotConfigured() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
            properties, () -> null, new AnnouncementRagDisabledEmbeddingModel(), null);

        infrastructure.initialize();

        assertEquals(AnnouncementRagState.NOT_CONFIGURED, infrastructure.status().state());
    }

    @Test
    void qdrantUnavailable_reportsUnavailableWithoutBlockingStartup() throws Exception {
        int unavailablePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            unavailablePort = socket.getLocalPort();
        }
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("test-key");
        try (var qdrantClient = new QdrantClient(
            QdrantGrpcClient.newBuilder("localhost", unavailablePort, false).build())) {
            AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
                properties, AnnouncementRagInfrastructureTest::embeddingConfig,
                new AnnouncementRagDisabledEmbeddingModel(), qdrantClient);

            infrastructure.initialize();

            assertEquals(AnnouncementRagState.UNAVAILABLE, infrastructure.status().state());
        }
    }

    @Test
    void disabledRag_doesNotInitializeVectorCapability() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        properties.setEnabled(false);
        AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
            properties, () -> null, new AnnouncementRagDisabledEmbeddingModel(), null);

        infrastructure.initialize();

        assertEquals(AnnouncementRagState.DISABLED, infrastructure.status().state());
    }
}
