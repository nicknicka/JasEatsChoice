package com.xx.jaseatschoicejava.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

class RagIndexEventListenerTest {

    private final RagKnowledgeService service = mock(RagKnowledgeService.class);
    private final RagIndexEventListener listener = new RagIndexEventListener(service);

    @Test
    void announcementChangeSyncsAnnouncementAfterCommit() {
        listener.onDocumentChanged(new RagDocumentChangedEvent(
            RagSourceType.ANNOUNCEMENT, "announcement-1", RagChangeType.UPSERT));

        verify(service).syncAnnouncement("announcement-1");
    }

    @Test
    void syncEventIsRegisteredForAfterCommitPhase() throws Exception {
        TransactionalEventListener annotation = RagIndexEventListener.class
            .getMethod("onDocumentChanged", RagDocumentChangedEvent.class)
            .getAnnotation(TransactionalEventListener.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
    }
}
