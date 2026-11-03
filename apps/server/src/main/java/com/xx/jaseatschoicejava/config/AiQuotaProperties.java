package com.xx.jaseatschoicejava.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * AI 饮食助手的用户级配额配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai-quota")
public class AiQuotaProperties {

    private QuotaLimit dishRecognition = new QuotaLimit(20, Duration.ofHours(1));
    private QuotaLimit recipe = new QuotaLimit(10, Duration.ofHours(1));

    @Data
    public static class QuotaLimit {

        private int limit;
        private Duration window;

        public QuotaLimit() {
        }

        public QuotaLimit(int limit, Duration window) {
            this.limit = limit;
            this.window = window;
        }
    }
}
