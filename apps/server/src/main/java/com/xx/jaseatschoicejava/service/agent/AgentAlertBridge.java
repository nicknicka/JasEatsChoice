package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentChannelConfig;
import com.xx.jaseatschoicejava.entity.AgentEvent;
import com.xx.jaseatschoicejava.mapper.AgentChannelConfigMapper;
import com.xx.jaseatschoicejava.mapper.AgentEventMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 只读 Agent Alert Bridge：轮询 dsh Management API，规范化并扇出通知。
 *
 * <p>规则：只读不写 dsh；事件按 ID 去重；通道失败只重试该通道，绝不影响 dsh Ticket。
 */
@Slf4j
@Service
public class AgentAlertBridge {

    @Autowired
    private AgentEventMapper eventMapper;

    @Autowired
    private AgentChannelConfigMapper channelConfigMapper;

    @Autowired
    private DshManagementClient dshManagementClient;

    @Autowired
    private List<NotificationChannel> channels;

    /**
     * 轮询一次：拉取 state/tickets，去重落库并把新事件扇出到各通道。返回新事件数。
     */
    public int pollOnce() {
        Map<String, Object> state = dshManagementClient.fetchState();
        List<Map<String, Object>> tickets = dshManagementClient.fetchTickets();
        int ingested = 0;
        for (AgentEvent event : AgentEventNormalizer.normalize(state, tickets)) {
            if (isDuplicate(event.getEventId())) {
                continue;
            }
            eventMapper.insert(event);
            notifyChannels(event, LocalTime.now());
            ingested++;
        }
        return ingested;
    }

    /**
     * 扇出到所有已配置通道；单通道失败被隔离并有限重试。
     */
    public void notifyChannels(AgentEvent event, LocalTime now) {
        for (NotificationChannel channel : channels) {
            AgentChannelConfig config = channelConfigMapper.selectById(channel.code());
            if (!shouldDispatch(config, event, now)) {
                continue;
            }
            int retryLimit = config.getRetryLimit() == null ? 0 : Math.max(config.getRetryLimit(), 0);
            for (int attempt = 0; attempt <= retryLimit; attempt++) {
                try {
                    channel.dispatch(event, config);
                    break;
                } catch (Exception e) {
                    if (attempt == retryLimit) {
                        log.warn("Agent 通知通道 {} 投递失败（已重试 {} 次），不影响 dsh Ticket：{}",
                                channel.code(), retryLimit, e.toString());
                    }
                }
            }
        }
    }

    private boolean isDuplicate(String eventId) {
        return eventId == null || eventMapper.selectById(eventId) != null;
    }

    /**
     * 通道是否应接收该事件：启用 + 订阅 + 严重级别 + 安静时段（critical 穿透安静时段）。
     */
    static boolean shouldDispatch(AgentChannelConfig config, AgentEvent event, LocalTime now) {
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            return false;
        }
        if (!subscribed(config.getEventTypes(), event.getEventType())) {
            return false;
        }
        if (severityRank(event.getSeverity()) < severityRank(config.getMinSeverity())) {
            return false;
        }
        if (!"critical".equalsIgnoreCase(event.getSeverity())
                && inQuietHours(config.getQuietStart(), config.getQuietEnd(), now)) {
            return false;
        }
        return true;
    }

    static boolean subscribed(String eventTypes, String eventType) {
        if (eventTypes == null || eventTypes.isBlank()) {
            return true;
        }
        for (String part : eventTypes.split(",")) {
            if (part.trim().equalsIgnoreCase(eventType)) {
                return true;
            }
        }
        return false;
    }

    static int severityRank(String severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity.toLowerCase()) {
            case "critical" -> 2;
            case "warning" -> 1;
            default -> 0;
        };
    }

    static boolean inQuietHours(String start, String end, LocalTime now) {
        if (start == null || end == null || start.isBlank() || end.isBlank()) {
            return false;
        }
        try {
            LocalTime s = LocalTime.parse(start.trim());
            LocalTime e = LocalTime.parse(end.trim());
            if (s.equals(e)) {
                return false;
            }
            if (s.isBefore(e)) {
                return !now.isBefore(s) && now.isBefore(e);
            }
            // 跨天时段，例如 22:00-07:00
            return !now.isBefore(s) || now.isBefore(e);
        } catch (Exception ignored) {
            return false;
        }
    }
}
