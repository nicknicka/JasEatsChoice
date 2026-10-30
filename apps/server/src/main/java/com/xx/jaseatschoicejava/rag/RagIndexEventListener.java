package com.xx.jaseatschoicejava.rag;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RagIndexEventListener {

    private final RagKnowledgeService ragKnowledgeService;

    public RagIndexEventListener(RagKnowledgeService ragKnowledgeService) {
        this.ragKnowledgeService = ragKnowledgeService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentChanged(RagDocumentChangedEvent event) {
        if (event.sourceType() != RagSourceType.ANNOUNCEMENT) {
            return;
        }
        ragKnowledgeService.syncAnnouncement(event.contentId());
    }
}
