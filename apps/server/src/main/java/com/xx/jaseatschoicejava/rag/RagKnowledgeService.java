package com.xx.jaseatschoicejava.rag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
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
    /**
     * 单次向量召回上限。有效期过滤在应用层完成（见 {@link #isEffectiveAt}），
     * 因此需要多取一些候选，避免过滤后可用结果不足。
     */
    private static final int MAX_SEARCH_RESULTS = 50;

    private final AnnouncementService announcementService;
    private final AnnouncementRagInfrastructure infrastructure;
    private final EmbeddingModel fixedEmbeddingModel;
    private final EmbeddingStore<TextSegment> fixedEmbeddingStore;

    /**
     * 生产路径每次都向基础设施要当前生效的 Embedding 客户端与 store，
     * 否则切换配置后检索仍会拿着构造期固定下来的旧引用。
     */
    public RagKnowledgeService(AnnouncementService announcementService, AnnouncementRagInfrastructure infrastructure) {
        this.announcementService = announcementService;
        this.infrastructure = infrastructure;
        this.fixedEmbeddingModel = null;
        this.fixedEmbeddingStore = null;
    }

    RagKnowledgeService(
        AnnouncementService announcementService,
        EmbeddingModel embeddingModel,
        EmbeddingStore<TextSegment> embeddingStore) {
        this.announcementService = announcementService;
        this.infrastructure = null;
        this.fixedEmbeddingModel = embeddingModel;
        this.fixedEmbeddingStore = embeddingStore;
    }

    private EmbeddingModel embeddingModel() {
        return infrastructure == null ? fixedEmbeddingModel : infrastructure.embeddingModel();
    }

    private EmbeddingStore<TextSegment> embeddingStore() {
        return infrastructure == null ? fixedEmbeddingStore : infrastructure.embeddingStore();
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
        if (embeddingStore() == null) {
            return 0;
        }
        List<Announcement> announcements = announcementService.list(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
        embeddingStore().removeAll(metadataKey("sourceType").isEqualTo("announcement"));
        announcements.forEach(this::synchronizeAnnouncement);
        return announcements.size();
    }

    private void synchronizeAnnouncement(Announcement announcement) {
        if (isIndexable(announcement)) {
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

        Embedding queryEmbedding = embeddingModel().embed(query.query()).content();
        EmbeddingStore<TextSegment> store = embeddingStore();
        if (store == null) {
            throw new IllegalStateException("公告 RAG 向量库暂不可用");
        }
        var result = store.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(MAX_SEARCH_RESULTS)
            .minScore(0d)
            .filter(visibilityFilter(query))
            .build());

        LocalDateTime now = query.now() == null ? LocalDateTime.now() : query.now();
        int topK = query.limit() <= 0 ? 3 : query.limit();
        LinkedHashMap<String, RagKnowledgeResult> bestByAnnouncement = new LinkedHashMap<>();
        for (var match : result.matches()) {
            RagKnowledgeResult candidate = toResult(match.embedded());
            if (!isEffectiveAt(candidate, now)) {
                continue;
            }
            bestByAnnouncement.putIfAbsent(candidate.announcementId(), candidate);
        }
        return bestByAnnouncement.values().stream().limit(topK).toList();
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
        List<Embedding> embeddings = embeddingModel().embedAll(segments).content();
        embeddingStore().removeAll(documentFilter(announcement.getId()));
        embeddingStore().addAll(pointIds, embeddings, segments);
    }

    private void deleteAnnouncement(String announcementId) {
        embeddingStore().removeAll(documentFilter(announcementId));
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
        metadata.put("contentHash", contentHash(announcement));
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
        List<Filter> merchantFilters = new ArrayList<>();
        merchantFilters.add(metadataKey("merchantId").isEqualTo("platform"));
        if (query.audience() == RagAudience.MERCHANT
            || (query.audience() == RagAudience.CUSTOMER && query.merchantId() != null)) {
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
            merchantScope
        );
        Filter result = filters.getFirst();
        for (int index = 1; index < filters.size(); index++) {
            result = result.and(filters.get(index));
        }
        return result;
    }

    /**
     * 有效期过滤放在应用层：langchain4j 的 Qdrant 过滤器只支持数值 range，
     * 而 startAt/endAt 是 Datetime 类型，放进向量检索条件会导致真实 Qdrant 报错。
     */
    private boolean isEffectiveAt(RagKnowledgeResult result, LocalDateTime now) {
        if (result.startTime() != null && now.isBefore(result.startTime())) {
            return false;
        }
        return result.endTime() == null || !now.isAfter(result.endTime());
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

    private boolean isIndexable(Announcement announcement) {
        if (!isActive(announcement)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return (announcement.getStartTime() == null || !now.isBefore(announcement.getStartTime()))
            && (announcement.getEndTime() == null || !now.isAfter(announcement.getEndTime()));
    }

    private String contentHash(Announcement announcement) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((announcement.getTitle() + "\n" + announcement.getContent())
                .getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(hash);
        }
        catch (Exception exception) {
            throw new IllegalStateException("无法计算公告内容哈希", exception);
        }
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
