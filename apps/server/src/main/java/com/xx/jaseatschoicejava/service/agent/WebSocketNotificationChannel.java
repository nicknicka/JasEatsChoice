package com.xx.jaseatschoicejava.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.AgentChannelConfig;
import com.xx.jaseatschoicejava.entity.AgentEvent;
import com.xx.jaseatschoicejava.websocket.WebSocketMessageService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WebSocket 通道：向配置的接收人推送一条 agent_event。
 */
@Component
public class WebSocketNotificationChannel implements NotificationChannel {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String code() {
        return "websocket";
    }

    @Override
    public void dispatch(AgentEvent event, AgentChannelConfig config) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "agent_event");
        payload.put("eventId", event.getEventId());
        payload.put("eventType", event.getEventType());
        payload.put("severity", event.getSeverity());
        payload.put("summary", event.getSummary());
        payload.put("actionRequired", event.getActionRequired());
        payload.put("ticketId", event.getTicketId());
        String json;
        try {
            json = MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("事件序列化失败", e);
        }
        for (String recipient : ChannelRecipients.parse(config)) {
            WebSocketMessageService.pushToUser(recipient, json);
        }
    }
}
