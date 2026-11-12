package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentChannelConfig;
import com.xx.jaseatschoicejava.entity.AgentEvent;
import com.xx.jaseatschoicejava.mapper.AgentChannelConfigMapper;
import com.xx.jaseatschoicejava.mapper.AgentEventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentAlertBridgeTest {

    @Mock
    private AgentEventMapper eventMapper;

    @Mock
    private AgentChannelConfigMapper channelConfigMapper;

    @Mock
    private DshManagementClient dshManagementClient;

    private AgentAlertBridge bridge;

    private static class RecordingChannel implements NotificationChannel {
        final String code;
        int calls = 0;
        boolean fail = false;

        RecordingChannel(String code) {
            this.code = code;
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public void dispatch(AgentEvent event, AgentChannelConfig config) {
            calls++;
            if (fail) {
                throw new IllegalStateException("channel down");
            }
        }
    }

    private static AgentChannelConfig enabledConfig(String code) {
        AgentChannelConfig config = new AgentChannelConfig();
        config.setChannelCode(code);
        config.setEnabled(true);
        config.setMinSeverity("info");
        config.setRetryLimit(1);
        config.setRecipients("dev-1");
        return config;
    }

    private static AgentEvent event(String severity) {
        AgentEvent event = new AgentEvent();
        event.setEventId("dsh:ticket:T-1:waiting_for_human");
        event.setEventType("waiting_for_human");
        event.setSeverity(severity);
        event.setSummary("dsh 任务 T-1：waiting_for_human");
        return event;
    }

    @BeforeEach
    void setUp() {
        bridge = new AgentAlertBridge();
        ReflectionTestUtils.setField(bridge, "eventMapper", eventMapper);
        ReflectionTestUtils.setField(bridge, "channelConfigMapper", channelConfigMapper);
        ReflectionTestUtils.setField(bridge, "dshManagementClient", dshManagementClient);
    }

    @Test
    void pollOnce_shouldDedupeExistingEvents() {
        when(dshManagementClient.fetchState()).thenReturn(Map.of(
                "health", Map.of("evals", Map.of("state", "error"))));
        when(dshManagementClient.fetchSources()).thenReturn(List.of(
                Map.of("id", "github", "state", "error")));
        // evals 组件与 github source 各一条事件；其中一条已存在 → 只新增 1。
        when(eventMapper.selectById("dsh:health:evals:error")).thenReturn(event("critical"));
        when(eventMapper.selectById("dsh:source:github:error")).thenReturn(null);
        ReflectionTestUtils.setField(bridge, "channels", List.<NotificationChannel>of());

        int ingested = bridge.pollOnce();

        assertEquals(1, ingested);
    }

    @Test
    void notifyChannels_shouldFanOutToAllConfiguredChannels() {
        RecordingChannel inApp = new RecordingChannel("in_app");
        RecordingChannel ws = new RecordingChannel("websocket");
        ReflectionTestUtils.setField(bridge, "channels", List.<NotificationChannel>of(inApp, ws));
        when(channelConfigMapper.selectById("in_app")).thenReturn(enabledConfig("in_app"));
        when(channelConfigMapper.selectById("websocket")).thenReturn(enabledConfig("websocket"));

        bridge.notifyChannels(event("warning"), LocalTime.NOON);

        assertEquals(1, inApp.calls);
        assertEquals(1, ws.calls);
    }

    @Test
    void notifyChannels_channelFailureShouldBeIsolated() {
        RecordingChannel broken = new RecordingChannel("in_app");
        broken.fail = true;
        RecordingChannel healthy = new RecordingChannel("websocket");
        ReflectionTestUtils.setField(bridge, "channels", List.<NotificationChannel>of(broken, healthy));
        when(channelConfigMapper.selectById("in_app")).thenReturn(enabledConfig("in_app"));
        when(channelConfigMapper.selectById("websocket")).thenReturn(enabledConfig("websocket"));

        bridge.notifyChannels(event("critical"), LocalTime.NOON);

        // 坏通道重试 2 次（retryLimit=1 → 2 次尝试），好通道照常投递，异常不冒泡。
        assertEquals(2, broken.calls);
        assertEquals(1, healthy.calls);
    }

    @Test
    void shouldDispatch_respectsSubscriptionSeverityAndQuietHours() {
        AgentChannelConfig config = enabledConfig("in_app");
        config.setEventTypes("ticket_failed");
        assertFalse(AgentAlertBridge.shouldDispatch(config, event("warning"), LocalTime.NOON));

        config.setEventTypes(null);
        config.setMinSeverity("critical");
        assertFalse(AgentAlertBridge.shouldDispatch(config, event("warning"), LocalTime.NOON));

        config.setMinSeverity("info");
        config.setQuietStart("22:00");
        config.setQuietEnd("07:00");
        assertFalse(AgentAlertBridge.shouldDispatch(config, event("warning"), LocalTime.of(23, 0)));
        // critical 穿透安静时段
        assertTrue(AgentAlertBridge.shouldDispatch(config, event("critical"), LocalTime.of(23, 0)));
    }

    @Test
    void shouldDispatch_disabledChannelNeverReceives() {
        AgentChannelConfig config = enabledConfig("in_app");
        config.setEnabled(false);
        assertFalse(AgentAlertBridge.shouldDispatch(config, event("critical"), LocalTime.NOON));
    }

    @Test
    void inQuietHours_handlesCrossMidnightWindow() {
        assertTrue(AgentAlertBridge.inQuietHours("22:00", "07:00", LocalTime.of(23, 30)));
        assertTrue(AgentAlertBridge.inQuietHours("22:00", "07:00", LocalTime.of(6, 0)));
        assertFalse(AgentAlertBridge.inQuietHours("22:00", "07:00", LocalTime.of(12, 0)));
    }
}
