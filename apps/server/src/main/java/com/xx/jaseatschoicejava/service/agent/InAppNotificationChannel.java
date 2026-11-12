package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentChannelConfig;
import com.xx.jaseatschoicejava.entity.AgentEvent;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import org.springframework.stereotype.Component;

/**
 * 站内消息中心通道：给配置的接收人各建一条系统通知。
 */
@Component
public class InAppNotificationChannel implements NotificationChannel {

    @Override
    public String code() {
        return "in_app";
    }

    @Override
    public void dispatch(AgentEvent event, AgentChannelConfig config) {
        for (String recipient : ChannelRecipients.parse(config)) {
            String content = event.getActionRequired() == null
                    ? event.getSummary()
                    : event.getSummary() + "；" + event.getActionRequired();
            NotificationUtil.createNotification(recipient, NotificationTypeEnum.SYSTEM,
                    "[Agent] " + event.getEventType(), content);
        }
    }
}
