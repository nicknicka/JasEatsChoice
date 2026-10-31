package com.xx.jaseatschoicejava.rag;

import java.time.LocalDateTime;

public record RagKnowledgeResult(
    String announcementId,
    String title,
    String text,
    int chunkIndex,
    int chunkCount,
    String merchantId,
    String targetAudience,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
}
