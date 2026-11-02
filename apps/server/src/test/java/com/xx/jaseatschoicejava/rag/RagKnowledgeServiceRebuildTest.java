package com.xx.jaseatschoicejava.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.service.AnnouncementService;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;

/**
 * 重建期间的行为：检索失败关闭，公告写入先记账、重建收尾补索引。
 */
class RagKnowledgeServiceRebuildTest {

    private final AnnouncementService announcementService = mock(AnnouncementService.class);
    private final AnnouncementRagInfrastructure infrastructure = mock(AnnouncementRagInfrastructure.class);
    private final RagKnowledgeService service =
        new RagKnowledgeService(announcementService, infrastructure);

    @Test
    void searchFailsClosedWhileRebuilding() {
        when(infrastructure.status()).thenReturn(new AnnouncementRagStatus(
            AnnouncementRagState.REBUILDING, "正在重建公告索引"));

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> service.search(
            new RagKnowledgeQuery("活动", RagAudience.CUSTOMER, null, LocalDateTime.now(), 3)));

        assertTrue(error.getMessage().contains("正在重建公告索引"));
        verify(infrastructure, never()).embeddingStore();
    }

    @Test
    void announcementChangedDuringRebuildIsIndexedWhenDrained() {
        when(infrastructure.status()).thenReturn(new AnnouncementRagStatus(
            AnnouncementRagState.REBUILDING, "正在重建公告索引"));
        Announcement announcement = announcement("a1");
        when(announcementService.getById("a1")).thenReturn(announcement);

        service.syncAnnouncement("a1");

        verify(infrastructure, never()).embeddingStore();

        when(infrastructure.status()).thenReturn(new AnnouncementRagStatus(
            AnnouncementRagState.READY, "公告索引重建完成"));
        EmbeddingStore<TextSegment> store = mock(EmbeddingStore.class);
        when(infrastructure.embeddingStore()).thenReturn(store);
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(infrastructure.embeddingModel()).thenReturn(model);
        when(model.embedAll(any())).thenReturn(Response.from(List.of(Embedding.from(new float[] {1f, 0f}))));

        assertEquals(1, service.drainPendingAnnouncements());
        verify(store).addAll(any(), any(), any());
    }

    @Test
    void announcementChangedAfterRebuildIsIndexedDirectly() {
        when(infrastructure.status()).thenReturn(new AnnouncementRagStatus(
            AnnouncementRagState.READY, "公告索引重建完成"));
        when(announcementService.getById("a2")).thenReturn(announcement("a2"));
        EmbeddingStore<TextSegment> store = mock(EmbeddingStore.class);
        when(infrastructure.embeddingStore()).thenReturn(store);
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(infrastructure.embeddingModel()).thenReturn(model);
        when(model.embedAll(any())).thenReturn(Response.from(List.of(Embedding.from(new float[] {1f, 0f}))));

        service.syncAnnouncement("a2");

        assertEquals(0, service.drainPendingAnnouncements());
        verify(store).addAll(any(), any(), any());
    }

    private Announcement announcement(String id) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setTitle("测试公告");
        announcement.setContent("内容");
        announcement.setStatus("active");
        announcement.setTargetAudience("all");
        return announcement;
    }
}
