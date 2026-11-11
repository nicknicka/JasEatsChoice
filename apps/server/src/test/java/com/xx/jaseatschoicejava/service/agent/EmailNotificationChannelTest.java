package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentChannelConfig;
import com.xx.jaseatschoicejava.entity.AgentEvent;
import com.xx.jaseatschoicejava.service.EmailService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailNotificationChannelTest {

    private static AgentChannelConfig config(String recipients) {
        AgentChannelConfig config = new AgentChannelConfig();
        config.setChannelCode("email");
        config.setRecipients(recipients);
        return config;
    }

    private static AgentEvent event() {
        AgentEvent event = new AgentEvent();
        event.setEventId("dsh:ticket:T-1:waiting_for_human");
        event.setEventType("waiting_for_human");
        event.setSeverity("warning");
        event.setTicketId("T-1");
        event.setSummary("dsh 任务 T-1：waiting_for_human");
        event.setActionRequired("请在 dsh 面板处理该任务");
        event.setLink("https://example.com/feedback/T-1");
        return event;
    }

    @Test
    void dispatch_shouldSendSanitizedBodyToEachRecipient() throws Exception {
        EmailService emailService = mock(EmailService.class);
        EmailNotificationChannel channel = new EmailNotificationChannel(emailService);

        channel.dispatch(event(), config("dev1@example.com,dev2@example.com"));

        verify(emailService).sendTextEmail(eq("dev1@example.com"), contains("waiting_for_human"), contains("T-1"));
        verify(emailService).sendTextEmail(eq("dev2@example.com"), contains("waiting_for_human"), contains("dsh 面板"));
    }

    @Test
    void renderBody_shouldOnlyUseSanitizedFields() {
        String body = EmailNotificationChannel.renderBody(event());
        assertTrue(body.contains("dsh 任务 T-1：waiting_for_human"));
        assertTrue(body.contains("后台引用：https://example.com/feedback/T-1"));
        assertFalse(body.toLowerCase().contains("attachment"));
        assertFalse(body.contains("@"));
    }

    @Test
    void dispatch_whenSendFails_shouldPropagateForBridgeIsolation() throws Exception {
        EmailService emailService = mock(EmailService.class);
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendTextEmail(eq("dev@example.com"), contains("Agent"), contains("T-1"));
        EmailNotificationChannel channel = new EmailNotificationChannel(emailService);

        assertThrows(IllegalStateException.class,
                () -> channel.dispatch(event(), config("dev@example.com")));
    }
}
