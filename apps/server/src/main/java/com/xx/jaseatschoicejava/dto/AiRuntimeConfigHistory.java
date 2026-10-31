package com.xx.jaseatschoicejava.dto;

import java.util.ArrayList;
import java.util.List;

import com.xx.jaseatschoicejava.config.AiModelConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 运行时配置版本历史。历史只保存非密钥配置和 key 指纹。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRuntimeConfigHistory {

    @Builder.Default
    private long activeVersion = 1L;

    private String activeUpdatedAt;

    @Builder.Default
    private List<Entry> history = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private long version;
        private String updatedAt;
        private AiModelConfig config;
        private boolean keyConfigured;
        private String keyFingerprint;
    }
}
