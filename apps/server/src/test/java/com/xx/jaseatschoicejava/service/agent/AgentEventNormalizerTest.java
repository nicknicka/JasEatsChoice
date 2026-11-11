package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentEventNormalizerTest {

    @Test
    void normalize_waitingTicket_shouldMapToWarningEvent() {
        List<AgentEvent> events = AgentEventNormalizer.normalize(
                Map.of(),
                List.of(Map.of("id", "T-1", "status", "waiting_for_human")));

        assertEquals(1, events.size());
        AgentEvent event = events.get(0);
        assertEquals("waiting_for_human", event.getEventType());
        assertEquals("warning", event.getSeverity());
        assertEquals("T-1", event.getTicketId());
        assertTrue(event.getSummary().contains("T-1"));
    }

    @Test
    void normalize_failedTicket_shouldBeCritical() {
        List<AgentEvent> events = AgentEventNormalizer.normalize(
                Map.of(),
                List.of(Map.of("id", "T-2", "status", "failed")));

        assertEquals("critical", events.get(0).getSeverity());
        assertEquals("ticket_failed", events.get(0).getEventType());
    }

    @Test
    void normalize_shouldNotCopyArbitraryContent() {
        // dsh 侧即便带了原始正文，规范化也只取 id/status，摘要由固定文案拼装。
        List<AgentEvent> events = AgentEventNormalizer.normalize(
                Map.of("raw", "13800000000 的订单 order-secret 报错"),
                List.of(Map.of("id", "T-3", "status", "blocked",
                        "title", "用户 13800000000 反馈", "body", "order-secret")));

        String serialized = events.toString();
        assertFalse(serialized.contains("13800000000"));
        assertFalse(serialized.contains("order-secret"));
    }

    @Test
    void normalize_stateWaitingFlag_shouldProduceStateEvent() {
        List<AgentEvent> events = AgentEventNormalizer.normalize(Map.of("waiting_for_human", true), List.of());
        assertEquals(1, events.size());
        assertEquals("waiting_for_human", events.get(0).getEventType());
    }
}
