package com.xx.jaseatschoicejava.rag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.config.AiRuntimeKeyStore;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AnnouncementRagStartupTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(AnnouncementRagConfig.class)
        .withBean("aiRuntimeKeyStore", AiRuntimeKeyStore.class, () -> new AiRuntimeKeyStore(""));

    @Test
    void disabledRag_startsInDisabledState() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=false",
                "zhipuai.api-key=test-key")
            .run(context -> {
                assertThat(context).hasSingleBean(AnnouncementRagInfrastructure.class);
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.DISABLED);
            });
    }

    @Test
    void missingEmbeddingKey_startsInNotConfiguredState() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.NOT_CONFIGURED);
            });
    }

    @Test
    void genericAiKey_configuresEmbeddingAndReachesQdrantStage() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=",
                "ai.api-key=generic-test-key",
                "announcement-rag.qdrant.host=localhost",
                "announcement-rag.qdrant.port=1")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.UNAVAILABLE);
            });
    }

    @Test
    void unavailableQdrant_startsInUnavailableState() {
        contextRunner
            .withPropertyValues(
                "announcement-rag.enabled=true",
                "zhipuai.api-key=test-key",
                "announcement-rag.qdrant.host=localhost",
                "announcement-rag.qdrant.port=1")
            .run(context -> {
                AnnouncementRagStatus status = context.getBean(AnnouncementRagInfrastructure.class).status();
                assertThat(status.state()).isEqualTo(AnnouncementRagState.UNAVAILABLE);
            });
    }
}
