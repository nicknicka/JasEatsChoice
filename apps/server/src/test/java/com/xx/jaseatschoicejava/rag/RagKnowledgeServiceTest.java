package com.xx.jaseatschoicejava.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.xx.jaseatschoicejava.entity.Announcement;
import com.xx.jaseatschoicejava.service.AnnouncementService;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

class RagKnowledgeServiceTest {

    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
    private final AnnouncementService announcementService = mock(AnnouncementService.class);
    private final RagKnowledgeService service = new RagKnowledgeService(
        announcementService, new DeterministicEmbeddingModel(), store);

    @Test
    void customerWithNamedMerchantScopeCannotSeeOtherMerchants() {
        Announcement platform = activePlatformAnnouncement();
        platform.setId("platform-customer");

        Announcement targetOwn = merchantAnnouncement("merchant-2", "customer");
        targetOwn.setId("target-own");

        Announcement otherOwn = merchantAnnouncement("merchant-3", "customer");
        otherOwn.setId("other-own");

        Announcement platformMerchant = activePlatformAnnouncement();
        platformMerchant.setId("platform-merchant");
        platformMerchant.setTargetAudience("merchant");

        when(announcementService.getById("platform-customer")).thenReturn(platform);
        when(announcementService.getById("target-own")).thenReturn(targetOwn);
        when(announcementService.getById("other-own")).thenReturn(otherOwn);
        when(announcementService.getById("platform-merchant")).thenReturn(platformMerchant);
        service.syncAnnouncement("platform-customer");
        service.syncAnnouncement("target-own");
        service.syncAnnouncement("other-own");
        service.syncAnnouncement("platform-merchant");

        List<RagKnowledgeResult> results = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, "merchant-2",
            LocalDateTime.parse("2026-10-31T12:00:00"), 10));

        assertThat(results).extracting(RagKnowledgeResult::announcementId)
            .containsExactlyInAnyOrder("platform-customer", "target-own");
    }

    @Test
    void activePlatformAnnouncement_isIndexedAndSearchableByCustomerWithoutMerchant() {
        Announcement announcement = activePlatformAnnouncement();
        when(announcementService.getById("announcement-1")).thenReturn(announcement);
        when(announcementService.list(any(Wrapper.class))).thenReturn(List.of(announcement));

        service.syncAnnouncement("announcement-1");

        List<RagKnowledgeResult> results = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 5));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().announcementId()).isEqualTo("announcement-1");
        assertThat(results.getFirst().title()).isEqualTo("Weekend discount");
        assertThat(results.getFirst().text()).contains("30 off 5");
    }

    @Test
    void update_replacesOldContentAndOldChunkIsNotReturned() {
        Announcement original = activePlatformAnnouncement();
        original.setTitle("OLD weekend discount");
        original.setContent("Old weekend discount text.");
        when(announcementService.getById("announcement-1")).thenReturn(original);
        service.syncAnnouncement("announcement-1");

        Announcement updated = activePlatformAnnouncement();
        when(announcementService.getById("announcement-1")).thenReturn(updated);
        service.syncAnnouncement("announcement-1");

        List<RagKnowledgeResult> results = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 5));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().text()).doesNotContain("Old");
        assertThat(results.getFirst().text()).contains("30 off 5");
    }

    @Test
    void deleteAndBatchDelete_removeAllChunksForEveryDocument() {
        Announcement first = activePlatformAnnouncement();
        Announcement second = merchantAnnouncement("merchant-2", "customer");
        when(announcementService.getById("announcement-1")).thenReturn(first);
        when(announcementService.getById("merchant-announcement-2")).thenReturn(second);
        service.syncAnnouncement("announcement-1");
        service.syncAnnouncement("merchant-announcement-2");

        when(announcementService.getById("announcement-1")).thenReturn(null);
        when(announcementService.getById("merchant-announcement-2")).thenReturn(null);
        service.syncAnnouncement("announcement-1");
        service.syncAnnouncement("merchant-announcement-2");

        List<RagKnowledgeResult> customer = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 5));
        List<RagKnowledgeResult> merchant = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.MERCHANT, "merchant-2", LocalDateTime.parse("2026-10-31T12:00:00"), 5));

        assertThat(customer).isEmpty();
        assertThat(merchant).isEmpty();
    }

    @Test
    void statusChangeToInactive_removesIndex() {
        when(announcementService.getById("announcement-1")).thenReturn(activePlatformAnnouncement());
        service.syncAnnouncement("announcement-1");

        Announcement inactive = activePlatformAnnouncement();
        inactive.setStatus("inactive");
        when(announcementService.getById("announcement-1")).thenReturn(inactive);
        service.syncAnnouncement("announcement-1");

        List<RagKnowledgeResult> results = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 5));

        assertThat(results).isEmpty();
    }

    @Test
    void announcementUnder800CharactersIsSingleChunk() {
        Announcement announcement = activePlatformAnnouncement();
        announcement.setContent("Weekend discount details. ".repeat(28).strip());
        when(announcementService.getById("announcement-1")).thenReturn(announcement);
        service.syncAnnouncement("announcement-1");

        List<RagKnowledgeResult> results = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 20));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().chunkCount()).isEqualTo(1);
    }

    @Test
    void longAnnouncementIsStableChunkedAndUpdateRemovesOldChunks() {
        Announcement original = activePlatformAnnouncement();
        original.setTitle("Weekend discount");
        original.setContent(("Spend 30 get 30 off 5 during the weekend. ".repeat(30)).strip());
        when(announcementService.getById("announcement-1")).thenReturn(original);
        service.syncAnnouncement("announcement-1");

        List<RagKnowledgeResult> originalResults = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 20));
        assertThat(originalResults.size()).isGreaterThan(1);
        assertThat(originalResults).extracting(RagKnowledgeResult::chunkCount)
            .containsOnly(originalResults.size());

        Announcement updated = activePlatformAnnouncement();
        when(announcementService.getById("announcement-1")).thenReturn(updated);
        service.syncAnnouncement("announcement-1");

        List<RagKnowledgeResult> updatedResults = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 20));
        assertThat(updatedResults).hasSize(1);
        assertThat(updatedResults.getFirst().chunkIndex()).isZero();
        assertThat(updatedResults.getFirst().chunkCount()).isEqualTo(1);
    }

    @Test
    void audienceAndMerchantScopeFilterSearchResults() {
        Announcement platformCustomer = activePlatformAnnouncement();
        Announcement platformMerchant = activePlatformAnnouncement();
        platformMerchant.setId("platform-merchant-announcement");
        platformMerchant.setTargetAudience("merchant");
        Announcement own = merchantAnnouncement("merchant-2", "merchant");
        when(announcementService.getById("announcement-1")).thenReturn(platformCustomer);
        when(announcementService.getById("platform-merchant-announcement")).thenReturn(platformMerchant);
        when(announcementService.getById("merchant-announcement-2")).thenReturn(own);
        service.syncAnnouncement("announcement-1");
        service.syncAnnouncement("platform-merchant-announcement");
        service.syncAnnouncement("merchant-announcement-2");

        LocalDateTime now = LocalDateTime.parse("2026-10-31T12:00:00");
        List<RagKnowledgeResult> customer = service.search(
            new RagKnowledgeQuery("weekend discount", RagAudience.CUSTOMER, null, now, 5));
        List<RagKnowledgeResult> merchant = service.search(
            new RagKnowledgeQuery("weekend discount", RagAudience.MERCHANT, "merchant-2", now, 5));

        assertThat(customer).extracting(RagKnowledgeResult::announcementId)
            .containsExactly("announcement-1");
        assertThat(merchant).extracting(RagKnowledgeResult::announcementId)
            .containsExactlyInAnyOrder("platform-merchant-announcement", "merchant-announcement-2");
    }

    @Test
    void allAudienceAnnouncementIsVisibleToCustomerAndMerchant() {
        Announcement platform = activePlatformAnnouncement();
        platform.setTargetAudience("all");
        Announcement own = merchantAnnouncement("merchant-2", "all");
        when(announcementService.getById("announcement-1")).thenReturn(platform);
        when(announcementService.getById("merchant-announcement-2")).thenReturn(own);
        service.syncAnnouncement("announcement-1");
        service.syncAnnouncement("merchant-announcement-2");

        LocalDateTime now = LocalDateTime.parse("2026-10-31T12:00:00");
        List<RagKnowledgeResult> customer = service.search(
            new RagKnowledgeQuery("weekend discount", RagAudience.CUSTOMER, null, now, 5));
        List<RagKnowledgeResult> merchant = service.search(
            new RagKnowledgeQuery("weekend discount", RagAudience.MERCHANT, "merchant-2", now, 5));

        assertThat(customer).extracting(RagKnowledgeResult::announcementId)
            .containsExactly("announcement-1");
        assertThat(merchant).extracting(RagKnowledgeResult::announcementId)
            .containsExactlyInAnyOrder("announcement-1", "merchant-announcement-2");
    }

    @Test
    void notStartedExpiredInactiveAndDeletedAnnouncementsAreNotReturned() {
        LocalDateTime now = LocalDateTime.parse("2026-10-31T12:00:00");
        Announcement active = activePlatformAnnouncement();
        Announcement notStarted = activePlatformAnnouncement();
        notStarted.setId("not-started");
        notStarted.setStartTime(LocalDateTime.parse("2026-11-01T00:00:00"));
        Announcement expired = activePlatformAnnouncement();
        expired.setId("expired");
        expired.setEndTime(LocalDateTime.parse("2026-10-31T00:00:00"));
        Announcement deleted = activePlatformAnnouncement();
        deleted.setId("deleted");
        when(announcementService.getById("announcement-1")).thenReturn(active);
        when(announcementService.getById("not-started")).thenReturn(notStarted);
        when(announcementService.getById("expired")).thenReturn(expired);
        when(announcementService.getById("deleted")).thenReturn(deleted);
        service.syncAnnouncement("announcement-1");
        service.syncAnnouncement("not-started");
        service.syncAnnouncement("expired");
        service.syncAnnouncement("deleted");

        when(announcementService.getById("deleted")).thenReturn(null);
        service.syncAnnouncement("deleted");

        List<RagKnowledgeResult> results = service.search(
            new RagKnowledgeQuery("weekend discount", RagAudience.CUSTOMER, null, now, 5));

        assertThat(results).extracting(RagKnowledgeResult::announcementId).containsExactly("announcement-1");
    }

    @Test
    void invalidMerchantScopeFailsClosed() {
        assertThat(service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.MERCHANT, null, LocalDateTime.parse("2026-10-31T12:00:00"), 5)))
            .isEmpty();
    }

    @Test
    void repeatedIndexingUsesStablePointIdsAndDoesNotDuplicate() {
        Announcement announcement = activePlatformAnnouncement();
        announcement.setContent("Spend 30 get 30 off 5 during the weekend.".repeat(30));
        when(announcementService.getById("announcement-1")).thenReturn(announcement);
        when(announcementService.list(any(Wrapper.class))).thenReturn(List.of(announcement));

        service.syncAnnouncement("announcement-1");
        service.syncAnnouncement("announcement-1");

        List<RagKnowledgeResult> results = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 20));

        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(result -> assertThat(result.announcementId()).isEqualTo("announcement-1"));
        assertThat(results.stream().map(RagKnowledgeResult::chunkIndex).distinct()).hasSize(results.size());
        int firstChunkCount = results.getFirst().chunkCount();
        service.rebuildAnnouncement("announcement-1");
        List<RagKnowledgeResult> rebuilt = service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 20));
        assertThat(rebuilt).hasSize(firstChunkCount);
        assertThat(rebuilt).extracting(RagKnowledgeResult::chunkIndex)
            .containsExactlyElementsOf(results.stream().map(RagKnowledgeResult::chunkIndex).toList());
    }

    @Test
    void fullRebuildIndexesAllAnnouncementsIdempotently() {
        Announcement platform = activePlatformAnnouncement();
        Announcement merchant = merchantAnnouncement("merchant-2", "merchant");
        when(announcementService.list(any(Wrapper.class))).thenReturn(List.of(platform, merchant));

        int firstCount = service.rebuildAllAnnouncements();
        int secondCount = service.rebuildAllAnnouncements();

        assertThat(firstCount).isEqualTo(2);
        assertThat(secondCount).isEqualTo(2);
        LocalDateTime now = LocalDateTime.parse("2026-10-31T12:00:00");
        assertThat(service.search(new RagKnowledgeQuery("weekend discount", RagAudience.CUSTOMER, null, now, 5)))
            .hasSize(1);
        assertThat(service.search(new RagKnowledgeQuery("weekend discount", RagAudience.MERCHANT, "merchant-2", now, 5)))
            .hasSize(1);
    }

    @Test
    void syncFailureDoesNotPropagateAfterAnnouncementWrite() {
        Announcement announcement = activePlatformAnnouncement();
        when(announcementService.getById("announcement-1")).thenReturn(announcement);
        EmbeddingModel failingModel = mock(EmbeddingModel.class);
        when(failingModel.embedAll(any())).thenThrow(new IllegalStateException("embedding failed"));
        RagKnowledgeService failingService = new RagKnowledgeService(announcementService, failingModel, store);

        assertThatCode(() -> failingService.syncAnnouncement("announcement-1")).doesNotThrowAnyException();
        assertThat(service.search(new RagKnowledgeQuery(
            "weekend discount", RagAudience.CUSTOMER, null, LocalDateTime.parse("2026-10-31T12:00:00"), 5)))
            .isEmpty();
    }

    private static Announcement merchantAnnouncement(String merchantId, String audience) {
        Announcement announcement = new Announcement();
        announcement.setId("merchant-announcement-2");
        announcement.setMerchantId(merchantId);
        announcement.setTitle("Weekend discount");
        announcement.setContent("Merchant 2 weekend discount text.");
        announcement.setTargetAudience(audience);
        announcement.setStatus("active");
        announcement.setStartTime(LocalDateTime.parse("2026-10-01T00:00:00"));
        announcement.setEndTime(LocalDateTime.parse("2026-11-30T23:59:59"));
        announcement.setUpdatedAt(LocalDateTime.parse("2026-10-30T10:00:00"));
        return announcement;
    }

    private static Announcement activePlatformAnnouncement() {
        Announcement announcement = new Announcement();
        announcement.setId("announcement-1");
        announcement.setMerchantId(null);
        announcement.setTitle("Weekend discount");
        announcement.setContent("Spend 30 get 30 off 5 during the weekend.");
        announcement.setTargetAudience("customer");
        announcement.setStatus("active");
        announcement.setStartTime(LocalDateTime.parse("2026-10-01T00:00:00"));
        announcement.setEndTime(LocalDateTime.parse("2026-11-30T23:59:59"));
        announcement.setUpdatedAt(LocalDateTime.parse("2026-10-30T10:00:00"));
        return announcement;
    }

    private static final class DeterministicEmbeddingModel implements EmbeddingModel {
        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
            List<Embedding> embeddings = textSegments.stream()
                .map(segment -> Embedding.from(vector(segment.text())))
                .toList();
            return Response.from(embeddings);
        }

        private float[] vector(String text) {
            String lower = text.toLowerCase();
            float discount = lower.contains("discount") || lower.contains("off") ? 1f : 0f;
            float weekend = lower.contains("weekend") ? 1f : 0f;
            float platform = 1f;
            return new float[] {discount, weekend, platform, 0f};
        }
    }
}
