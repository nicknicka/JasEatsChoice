package com.xx.jaseatschoicejava.rag;

import java.util.ArrayList;
import java.util.List;

class AnnouncementRagChunker {

    private static final int MAX_BODY_LENGTH = 500;
    private static final int OVERLAP_LENGTH = 80;

    private AnnouncementRagChunker() {
    }

    static List<String> chunk(String title, String content) {
        String normalizedContent = content == null ? "" : content.strip();
        String normalizedTitle = title == null ? "" : title.strip();
        if (normalizedContent.isEmpty()) {
            return List.of(normalizedTitle);
        }

        List<String> bodyChunks = splitParagraphs(normalizedContent);
        return bodyChunks.stream()
            .map(bodyChunk -> normalizedTitle + "\n" + bodyChunk)
            .toList();
    }

    private static List<String> splitParagraphs(String content) {
        List<String> chunks = new ArrayList<>();
        for (String paragraph : content.split("\\n\\s*\\n")) {
            String normalizedParagraph = paragraph.strip();
            if (normalizedParagraph.isEmpty()) {
                continue;
            }
            if (normalizedParagraph.length() <= MAX_BODY_LENGTH) {
                chunks.add(normalizedParagraph);
                continue;
            }
            chunks.addAll(splitLongParagraph(normalizedParagraph));
        }
        return chunks;
    }

    private static List<String> splitLongParagraph(String paragraph) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < paragraph.length()) {
            int end = Math.min(start + MAX_BODY_LENGTH, paragraph.length());
            if (end < paragraph.length()) {
                int sentenceEnd = Math.max(
                    paragraph.lastIndexOf('。', end),
                    paragraph.lastIndexOf('.', end));
                if (sentenceEnd > start + MAX_BODY_LENGTH / 2) {
                    end = sentenceEnd + 1;
                }
            }
            chunks.add(paragraph.substring(start, end).strip());
            if (end >= paragraph.length()) {
                break;
            }
            start = Math.max(end - OVERLAP_LENGTH, start + 1);
        }
        return chunks;
    }
}
