package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把 dsh Management API 的 state / sources 规范化为 AgentEvent。
 *
 * <p>形状对齐 dsh-superagent-prototype：state 里是 health（逐组件状态）与 paused，
 * sources 是每个 Source 的 state。prototype 没有 waiting_for_human 字段，也没有稳定的
 * tickets 列表，人工介入与系统问题从这两处派生。
 *
 * <p>只使用标识与状态字段，**从不复制任意文本字段**：摘要由固定文案拼装，
 * 保证事件里不带上 dsh 侧可能存在的原始内容。
 */
public final class AgentEventNormalizer {

    private AgentEventNormalizer() {
    }

    public static List<AgentEvent> normalize(Map<String, Object> state, List<Map<String, Object>> sources) {
        List<AgentEvent> events = new ArrayList<>();
        appendHealthErrors(events, state);
        appendPaused(events, state);
        appendSourceProblems(events, sources);
        return events;
    }

    /** state.health：每个组件带 state（starting/ready/paused/error）；只对 error 报警。 */
    private static void appendHealthErrors(List<AgentEvent> events, Map<String, Object> state) {
        if (state == null || !(state.get("health") instanceof Map<?, ?> health)) {
            return;
        }
        for (Map.Entry<?, ?> part : health.entrySet()) {
            if (!(part.getValue() instanceof Map<?, ?> entry)) {
                continue;
            }
            if (!"error".equalsIgnoreCase(text(entry.get("state")))) {
                continue;
            }
            String name = String.valueOf(part.getKey());
            events.add(build("dsh:health:" + name + ":error", "system_error", "critical", null,
                    "dsh 组件 " + name + " 报错", "请到 dsh 面板处理该组件", null));
        }
    }

    /** state.paused：处理被暂停，不派发、不评估、不回复。 */
    private static void appendPaused(List<AgentEvent> events, Map<String, Object> state) {
        if (state != null && Boolean.TRUE.equals(state.get("paused"))) {
            events.add(build("dsh:state:paused", "processing_paused", "warning", null,
                    "dsh 处理已暂停", "请在 dsh 面板恢复处理", null));
        }
    }

    /** sources[].state：error / missing / setup 属异常；off / starting / paused / ready 不算。 */
    private static void appendSourceProblems(List<AgentEvent> events, List<Map<String, Object>> sources) {
        if (sources == null) {
            return;
        }
        for (Map<String, Object> source : sources) {
            if (source == null) {
                continue;
            }
            String id = firstText(source, "id", "kind", "name");
            String status = text(source.get("state"));
            if (id == null || status == null) {
                continue;
            }
            String normalized = status.toLowerCase();
            if (!isProblem(normalized)) {
                continue;
            }
            boolean severe = "error".equals(normalized) || "missing".equals(normalized);
            events.add(build("dsh:source:" + id + ":" + normalized,
                    "source_problem", severe ? "critical" : "warning", null,
                    "dsh Source " + id + "：" + status,
                    "请到 dsh 面板检查该 Source", null));
        }
    }

    private static boolean isProblem(String status) {
        return "error".equals(status) || "missing".equals(status) || "setup".equals(status);
    }

    private static AgentEvent build(String eventId, String type, String severity, String ticketId,
                                     String summary, String actionRequired, String link) {
        AgentEvent event = new AgentEvent();
        event.setEventId(eventId);
        event.setEventType(type);
        event.setSeverity(severity);
        event.setTicketId(ticketId);
        event.setSummary(summary);
        event.setActionRequired(actionRequired);
        event.setLink(link);
        event.setOccurredAt(LocalDateTime.now());
        return event;
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String firstText(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }
}
