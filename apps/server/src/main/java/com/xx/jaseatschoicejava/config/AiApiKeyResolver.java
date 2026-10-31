package com.xx.jaseatschoicejava.config;

/**
 * 统一解析智谱 AI key：供应商专属 key 优先，通用 AI key 兜底。
 */
public final class AiApiKeyResolver {

    private AiApiKeyResolver() {
    }

    public static String resolve(ZhipuAIConfig zhipuAIConfig, AIConfig aiConfig) {
        String providerKey = zhipuAIConfig != null ? zhipuAIConfig.getApiKey() : null;
        if (hasText(providerKey)) {
            return providerKey;
        }
        String genericKey = aiConfig != null ? aiConfig.getApiKey() : null;
        return hasText(genericKey) ? genericKey : "";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
