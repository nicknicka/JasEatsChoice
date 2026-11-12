package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentChannelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 通道接收人解析：配置里用逗号分隔的用户ID。
 */
final class ChannelRecipients {

    private ChannelRecipients() {
    }

    static List<String> parse(AgentChannelConfig config) {
        List<String> recipients = new ArrayList<>();
        if (config == null || config.getRecipients() == null) {
            return recipients;
        }
        for (String part : config.getRecipients().split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                recipients.add(trimmed);
            }
        }
        return recipients;
    }
}
