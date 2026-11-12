package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把 dsh state / tickets 规范化为 AgentEvent。
 *
 * <p>只使用标识与状态字段，**从不复制任意文本字段**：摘要由固定文案拼装，
 * 保证事件里不带上 dsh 侧可能存在的原始内容。
 */
public final class AgentEventNormalizer {

    private AgentEventNormalizer() {
    }

    public static List<AgentEvent> normalize(Map<String, Object> state, List<Map<String, Object>> tickets) {
        List<AgentEvent> events = new ArrayList<>();
        if (state != null && Boolean.TRUE.equals(state.get("waiting_for_human"))) {
            events.add(build("dsh:state:waiting_for_human", "waiting_for_human", "warning",
                    null, "dsh 正在等待人工介入", "请到 dsh 面板处理等待中的任务", null));
        }
        if (tickets != null) {
            for (Map<String, Object> ticket : tickets) {
                if (ticket == null) {
                    continue;
                }
                String ticketId = firstText(ticket, "id", "ticket_id", "ticketId");
                String status = firstText(ticket, "status", "state");
                if (ticketId == null || status == null) {
                    continue;
                }
                String normalizedStatus = status.toLowerCase();
                events.add(build(
                        "dsh:ticket:" + ticketId + ":" + normalizedStatus,
                        typeOf(normalizedStatus),
                        severityOf(normalizedStatus),
                        ticketId,
                        "dsh 任务 " + ticketId + "：" + status,
                        actionOf(normalizedStatus),
                        null));
            }
        }
        return events;
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

    private static String typeOf(String status) {
        if (isWaiting(status)) {
            return "waiting_for_human";
        }
        if (isFailed(status)) {
            return "ticket_failed";
        }
        return "state_changed";
    }

    private static String severityOf(String status) {
        if (isFailed(status)) {
            return "critical";
        }
        if (isWaiting(status)) {
            return "warning";
        }
        return "info";
    }

    private static String actionOf(String status) {
        if (isWaiting(status)) {
            return "请在 dsh 面板处理该任务";
        }
        if (isFailed(status)) {
            return "请检查失败原因并决定重试或回滚";
        }
        return null;
    }

    private static boolean isWaiting(String status) {
        return status.contains("waiting") || status.contains("human") || status.contains("blocked");
    }

    private static boolean isFailed(String status) {
        return status.contains("fail") || status.contains("error");
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
