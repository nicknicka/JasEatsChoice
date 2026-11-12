package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentChannelConfig;
import com.xx.jaseatschoicejava.entity.AgentEvent;
import com.xx.jaseatschoicejava.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 邮件通知通道：实现统一 {@link NotificationChannel}，正文只含脱敏摘要与引用链接。
 *
 * <p>邮件不是任务事实来源；发送失败由桥接层重试并隔离，不回写 dsh 状态。
 */
@Slf4j
@Component
public class EmailNotificationChannel implements NotificationChannel {

    private final EmailService emailService;

    public EmailNotificationChannel(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public String code() {
        return "email";
    }

    @Override
    public void dispatch(AgentEvent event, AgentChannelConfig config) {
        String subject = "[Agent][" + event.getSeverity() + "] " + event.getEventType();
        String body = renderBody(event);
        for (String recipient : ChannelRecipients.parse(config)) {
            try {
                emailService.sendTextEmail(recipient, subject, body);
            } catch (Exception e) {
                // 交给桥接层统一重试与隔离；这里只抛出，不再吞掉。
                throw new IllegalStateException("邮件投递失败：" + recipient + "：" + e.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 正文只使用事件里的脱敏字段；不包含反馈正文、附件、联系方式或身份信息。
     */
    static String renderBody(AgentEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("摘要：").append(nullSafe(event.getSummary())).append('\n');
        if (event.getActionRequired() != null && !event.getActionRequired().isBlank()) {
            sb.append("需要处理：").append(event.getActionRequired()).append('\n');
        }
        if (event.getTicketId() != null && !event.getTicketId().isBlank()) {
            sb.append("任务：").append(event.getTicketId()).append('\n');
        }
        if (event.getLink() != null && !event.getLink().isBlank()) {
            sb.append("后台引用：").append(event.getLink()).append('\n');
        }
        sb.append('\n');
        sb.append("GitHub Issue 评论仍是该任务的事实来源，邮件仅作提醒。\n");
        return sb.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
