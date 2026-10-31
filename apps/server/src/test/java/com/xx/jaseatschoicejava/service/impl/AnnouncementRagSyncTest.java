package com.xx.jaseatschoicejava.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.mapper.AnnouncementMapper;
import com.xx.jaseatschoicejava.rag.RagChangeType;
import com.xx.jaseatschoicejava.rag.RagDocumentChangedEvent;
import com.xx.jaseatschoicejava.rag.RagSourceType;

class AnnouncementRagSyncTest {

    private final AnnouncementMapper mapper = mock(AnnouncementMapper.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final AnnouncementServiceImpl service = new AnnouncementServiceImpl(publisher);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void savePublishesUpsertEvent() {
        Announcement announcement = new Announcement();
        announcement.setId("announcement-1");
        when(mapper.insert(any(Announcement.class))).thenReturn(1);

        boolean saved = service.save(announcement);

        assertThat(saved).isTrue();
        verify(publisher).publishEvent(new RagDocumentChangedEvent(
            RagSourceType.ANNOUNCEMENT, "announcement-1", RagChangeType.UPSERT));
    }

    @Test
    void updateByIdPublishesUpsertEvent() {
        Announcement announcement = new Announcement();
        announcement.setId("announcement-1");
        when(mapper.updateById(any(Announcement.class))).thenReturn(1);

        boolean updated = service.updateById(announcement);

        assertThat(updated).isTrue();
        verify(publisher).publishEvent(new RagDocumentChangedEvent(
            RagSourceType.ANNOUNCEMENT, "announcement-1", RagChangeType.UPSERT));
    }

    @Test
    void updateBatchByIdPublishesUpsertEventForEachAnnouncement() {
        Announcement announcement = new Announcement();
        announcement.setId("announcement-1");
        when(mapper.updateById(any(Announcement.class))).thenReturn(1);

        boolean updated = service.updateBatchById(java.util.List.of(announcement));

        assertThat(updated).isTrue();
        verify(publisher).publishEvent(new RagDocumentChangedEvent(
            RagSourceType.ANNOUNCEMENT, "announcement-1", RagChangeType.UPSERT));
    }

    @Test
    void removeByIdPublishesDeleteEvent() {
        when(mapper.deleteById("announcement-1")).thenReturn(1);

        boolean removed = service.removeById("announcement-1");

        assertThat(removed).isTrue();
        verify(publisher).publishEvent(new RagDocumentChangedEvent(
            RagSourceType.ANNOUNCEMENT, "announcement-1", RagChangeType.DELETE));
    }

    @Test
    void removeBatchByIdsPublishesDeleteEventForEachId() {
        when(mapper.deleteById("announcement-1")).thenReturn(1);
        when(mapper.deleteById("announcement-2")).thenReturn(1);

        boolean removed = service.removeBatchByIds(java.util.List.of("announcement-1", "announcement-2"));

        assertThat(removed).isTrue();
        verify(publisher).publishEvent(new RagDocumentChangedEvent(
            RagSourceType.ANNOUNCEMENT, "announcement-1", RagChangeType.DELETE));
        verify(publisher).publishEvent(new RagDocumentChangedEvent(
            RagSourceType.ANNOUNCEMENT, "announcement-2", RagChangeType.DELETE));
    }
}
