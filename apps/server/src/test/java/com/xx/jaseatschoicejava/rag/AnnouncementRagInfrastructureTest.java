package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.ServerSocket;
import org.junit.jupiter.api.Test;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

class AnnouncementRagInfrastructureTest {

    private static AnnouncementEmbeddingConfig embeddingConfig() {
        return new AnnouncementEmbeddingConfig("openai-compatible", "bge-m3", 2048, "");
    }

    @Test
    void missingEmbeddingKey_reportsNotConfigured() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
            properties, () -> null, AnnouncementRagDisabledEmbeddingModel::new, null);

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
        try (var qdrantClient = new QdrantClient(
            QdrantGrpcClient.newBuilder("localhost", unavailablePort, false).build())) {
            AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
                properties, AnnouncementRagInfrastructureTest::embeddingConfig,
                AnnouncementRagDisabledEmbeddingModel::new, qdrantClient);

            infrastructure.initialize();

            assertEquals(AnnouncementRagState.UNAVAILABLE, infrastructure.status().state());
        }
    }

    @Test
    void disabledRag_doesNotInitializeVectorCapability() {
        AnnouncementRagProperties properties = new AnnouncementRagProperties();
        properties.setEnabled(false);
        AnnouncementRagInfrastructure infrastructure = new AnnouncementRagInfrastructure(
            properties, () -> null, AnnouncementRagDisabledEmbeddingModel::new, null);

        infrastructure.initialize();

        assertEquals(AnnouncementRagState.DISABLED, infrastructure.status().state());
    }
}
