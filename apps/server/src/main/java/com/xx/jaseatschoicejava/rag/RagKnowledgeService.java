package com.xx.jaseatschoicejava.rag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.service.AnnouncementService;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

public class RagKnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(RagKnowledgeService.class);
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final AnnouncementService announcementService;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public RagKnowledgeService(AnnouncementService announcementService, AnnouncementRagInfrastructure infrastructure) {
        this(announcementService, infrastructure.embeddingModel(), infrastructure.embeddingStore());
    }

    RagKnowledgeService(
        AnnouncementService announcementService,
        EmbeddingModel embeddingModel,
        EmbeddingStore<TextSegment> embeddingStore) {
        this.announcementService = announcementService;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    public void syncAnnouncement(String announcementId) {
        try {
            Announcement announcement = announcementService.getById(announcementId);
            if (announcement == null) {
                deleteAnnouncement(announcementId);
                return;
            }
            synchronizeAnnouncement(announcement);
        }
        catch (Exception exception) {
            log.error("公告 RAG 同步失败: sourceType=announcement, contentId={}", announcementId, exception);
        }
    }

    public void rebuildAnnouncement(String announcementId) {
        syncAnnouncement(announcementId);
    }

    public int rebuildAllAnnouncements() {
        List<Announcement> announcements = announcementService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
        announcements.forEach(this::synchronizeAnnouncement);
        return announcements.size();
    }

    private void synchronizeAnnouncement(Announcement announcement) {
        if (isActive(announcement)) {
            indexAnnouncement(announcement);
        }
        else {
            deleteAnnouncement(announcement.getId());
        }
    }

    public List<RagKnowledgeResult> search(RagKnowledgeQuery query) {
        if (query == null || query.query() == null || query.query().isBlank()) {
            return List.of();
        }
        if (query.audience() == null || (query.audience() == RagAudience.MERCHANT
            && (query.merchantId() == null || query.merchantId().isBlank()))) {
            return List.of();
        }

        Embedding queryEmbedding = embeddingModel.embed(query.query()).content();
        var result = embeddingStore.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(query.limit() <= 0 ? 5 : query.limit())
            .minScore(0d)
            .filter(visibilityFilter(query))
            .build());

        return result.matches().stream()
            .map(match -> toResult(match.embedded()))
            .toList();
    }

    private void indexAnnouncement(Announcement announcement) {
        List<String> chunks = chunkText(announcement.getTitle(), announcement.getContent());
        List<String> pointIds = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            pointIds.add(pointId(announcement.getId(), index));
        }
        List<Metadata> metadata = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            metadata.add(metadata(announcement, index, chunks.size(), chunks.get(index)));
        }
        List<TextSegment> segments = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            segments.add(TextSegment.from(chunks.get(index), metadata.get(index)));
        }
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.removeAll(documentFilter(announcement.getId()));
        embeddingStore.addAll(pointIds, embeddings, segments);
    }

    private void deleteAnnouncement(String announcementId) {
        embeddingStore.removeAll(documentFilter(announcementId));
    }

    private String pointId(String announcementId, int chunkIndex) {
        return UUID.nameUUIDFromBytes(("announcement:" + announcementId + ":" + chunkIndex)
            .getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
    }

    private Metadata metadata(Announcement announcement, int chunkIndex, int chunkCount, String chunk) {
        Metadata metadata = new Metadata();
        metadata.put("sourceType", "announcement");
        metadata.put("contentId", announcement.getId());
        metadata.put("documentId", "announcement:" + announcement.getId());
        metadata.put("chunkIndex", chunkIndex);
        metadata.put("chunkCount", chunkCount);
        metadata.put("title", announcement.getTitle());
        metadata.put("text", chunk);
        metadata.put("merchantId", announcement.getMerchantId() == null ? "platform" : announcement.getMerchantId());
        metadata.put("targetAudience", announcement.getTargetAudience());
        metadata.put("status", announcement.getStatus());
        metadata.put("startAt", datetime(announcement.getStartTime()));
        metadata.put("endAt", datetime(announcement.getEndTime()));
        metadata.put("updatedAt", datetime(announcement.getUpdatedAt()));
        metadata.put("audienceTags", announcement.getTargetAudience());
        return metadata;
    }

    private Filter visibilityFilter(RagKnowledgeQuery query) {
        String now = datetime(query.now());
        List<Filter> merchantFilters = new ArrayList<>();
        merchantFilters.add(metadataKey("merchantId").isEqualTo("platform"));
        if (query.audience() == RagAudience.MERCHANT) {
            merchantFilters.add(metadataKey("merchantId").isEqualTo(query.merchantId()));
        }
        Filter merchantScope = merchantFilters.getFirst();
        for (int index = 1; index < merchantFilters.size(); index++) {
            merchantScope = merchantScope.or(merchantFilters.get(index));
        }

        List<Filter> filters = List.of(
            metadataKey("sourceType").isEqualTo("announcement"),
            metadataKey("status").isEqualTo("active"),
            metadataKey("audienceTags").isIn(java.util.List.of("all", query.audience().name().toLowerCase())),
            merchantScope,
            timeFilter("startAt", now),
            endTimeFilter(now)
        );
        Filter result = filters.getFirst();
        for (int index = 1; index < filters.size(); index++) {
            result = result.and(filters.get(index));
        }
        return result;
    }

    private Filter timeFilter(String key, String now) {
        return Filter.or(
            metadataKey(key).isEqualTo(""),
            metadataKey(key).isLessThanOrEqualTo(now)
        );
    }

    private Filter endTimeFilter(String now) {
        return Filter.or(
            metadataKey("endAt").isEqualTo(""),
            metadataKey("endAt").isGreaterThanOrEqualTo(now)
        );
    }

    private Filter documentFilter(String announcementId) {
        return Filter.and(
            metadataKey("sourceType").isEqualTo("announcement"),
            metadataKey("contentId").isEqualTo(announcementId)
        );
    }

    private RagKnowledgeResult toResult(TextSegment segment) {
        Metadata metadata = segment.metadata();
        return new RagKnowledgeResult(
            metadata.getString("contentId"),
            metadata.getString("title"),
            metadata.getString("text"),
            metadata.getInteger("chunkIndex"),
            metadata.getInteger("chunkCount"),
            metadata.getString("merchantId"),
            metadata.getString("targetAudience"),
            parseDatetime(metadata.getString("startAt")),
            parseDatetime(metadata.getString("endAt"))
        );
    }

    private boolean isActive(Announcement announcement) {
        return "active".equals(announcement.getStatus());
    }

    private List<String> chunkText(String title, String content) {
        return AnnouncementRagChunker.chunk(title, content);
    }

    private String datetime(LocalDateTime value) {
        return value == null ? "" : DATETIME.format(value);
    }

    private LocalDateTime parseDatetime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value, DATETIME);
    }
}
