package com.xx.jaseatschoicejava.rag;

import java.time.LocalDateTime;

public record RagKnowledgeQuery(
    String query,
    RagAudience audience,
    String merchantId,
    LocalDateTime now,
    int limit
) {
}
