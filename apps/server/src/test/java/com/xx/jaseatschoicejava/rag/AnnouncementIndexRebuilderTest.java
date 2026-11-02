package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

/**
 * 索引重建编排：重建中 → 就绪、失败停在不可用并可重试、重复请求不并发重建。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnnouncementIndexRebuilderTest {

    @Mock
    private AnnouncementRagInfrastructure infrastructure;

    @Mock
    private RagKnowledgeService knowledgeService;

    private AnnouncementIndexRebuilder rebuilder;

    @BeforeEach
    void setUp() {
        rebuilder = new AnnouncementIndexRebuilder(infrastructure, knowledgeService, Runnable::run);
    }

    @Test
    void successfulRebuildMarksReadyAndDrainsPendingWrites() {
        EmbeddingStore<TextSegment> store = mock(EmbeddingStore.class);
        when(infrastructure.embeddingStore()).thenReturn(store);
        when(knowledgeService.rebuildAllAnnouncements()).thenReturn(3);

        int count = rebuilder.rebuildNow();

        assertEquals(3, count);
        InOrder inOrder = inOrder(infrastructure, knowledgeService);
        inOrder.verify(infrastructure).markRebuilding(anyString());
        inOrder.verify(knowledgeService).rebuildAllAnnouncements();
        inOrder.verify(infrastructure).markReady(anyString());
        inOrder.verify(knowledgeService).drainPendingAnnouncements();
        verify(infrastructure, never()).markUnavailable(anyString());
    }

    @Test
    void failedRebuildStopsUnavailableWithReason() {
        EmbeddingStore<TextSegment> store = mock(EmbeddingStore.class);
        when(infrastructure.embeddingStore()).thenReturn(store);
        when(knowledgeService.rebuildAllAnnouncements())
            .thenThrow(new RuntimeException("Qdrant 连接被拒绝"));

        int count = rebuilder.rebuildNow();

        assertEquals(0, count);
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(infrastructure).markUnavailable(reason.capture());
        assertTrue(reason.getValue().contains("Qdrant 连接被拒绝"));
        verify(infrastructure, never()).markReady(anyString());
    }

    @Test
    void retryAfterFailureReachesReady() {
        EmbeddingStore<TextSegment> store = mock(EmbeddingStore.class);
        when(infrastructure.embeddingStore()).thenReturn(store);
        when(knowledgeService.rebuildAllAnnouncements())
            .thenThrow(new RuntimeException("首次失败"))
            .thenReturn(1);

        assertEquals(0, rebuilder.rebuildNow());
        assertEquals(1, rebuilder.rebuildNow());

        verify(infrastructure).markReady(anyString());
    }

    @Test
    void rebuildWithoutVectorStoreStopsUnavailableWithExistingReason() {
        when(infrastructure.embeddingStore()).thenReturn(null);
        when(infrastructure.status()).thenReturn(new AnnouncementRagStatus(
            AnnouncementRagState.NOT_CONFIGURED, "公告 RAG Embedding 未配置"));

        assertEquals(0, rebuilder.rebuildNow());

        verify(infrastructure).markUnavailable("公告 RAG Embedding 未配置");
    }

    @Test
    void requestRebuildMarksRebuildingBeforeDispatchAndDeduplicates() {
        List<Runnable> queued = new ArrayList<>();
        AnnouncementIndexRebuilder async = new AnnouncementIndexRebuilder(
            infrastructure, knowledgeService, queued::add);

        async.requestRebuild();
        async.requestRebuild();

        assertEquals(1, queued.size());
        verify(infrastructure).markRebuilding("正在重建公告索引");
    }
}
