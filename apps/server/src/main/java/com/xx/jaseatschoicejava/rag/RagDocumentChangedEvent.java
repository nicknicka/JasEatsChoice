package com.xx.jaseatschoicejava.rag;

public record RagDocumentChangedEvent(
    RagSourceType sourceType,
    String contentId,
    RagChangeType changeType
) {
}
