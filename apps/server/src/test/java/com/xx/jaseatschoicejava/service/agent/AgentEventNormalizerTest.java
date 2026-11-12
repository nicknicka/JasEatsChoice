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
    void healthError_becomesCriticalSystemErrorAndHidesRawText() {
        Map<String, Object> state = Map.of("health", Map.of(
                "evals", Map.of("state", "error", "detail", "boom"),
                "sources", Map.of("state", "ready")));

        List<AgentEvent> events = AgentEventNormalizer.normalize(state, List.of());

        assertEquals(1, events.size());
        AgentEvent e = events.get(0);
        assertEquals("dsh:health:evals:error", e.getEventId());
        assertEquals("system_error", e.getEventType());
        assertEquals("critical", e.getSeverity());
        // 不复制 dsh 侧原始文本
        assertFalse(e.getSummary().contains("boom"));
    }

    @Test
    void paused_becomesWarning() {
        List<AgentEvent> events = AgentEventNormalizer.normalize(Map.of("paused", true), List.of());

        assertEquals(1, events.size());
        assertEquals("dsh:state:paused", events.get(0).getEventId());
        assertEquals("warning", events.get(0).getSeverity());
    }

    @Test
    void sourceProblems_onlyForProblemStates() {
        List<Map<String, Object>> sources = List.of(
                Map.of("id", "github", "state", "error"),
                Map.of("id", "feishu", "state", "ready"),
                Map.of("id", "weird", "state", "setup"),
                Map.of("id", "off-one", "state", "off"));

        List<AgentEvent> events = AgentEventNormalizer.normalize(Map.of(), sources);

        assertEquals(2, events.size());
        List<String> ids = events.stream().map(AgentEvent::getEventId).toList();
        assertTrue(ids.contains("dsh:source:github:error"));
        assertTrue(ids.contains("dsh:source:weird:setup"));
    }

    @Test
    void emptyInputs_produceNoEvents() {
        assertTrue(AgentEventNormalizer.normalize(null, null).isEmpty());
        assertTrue(AgentEventNormalizer.normalize(Map.of(), List.of()).isEmpty());
    }
}
