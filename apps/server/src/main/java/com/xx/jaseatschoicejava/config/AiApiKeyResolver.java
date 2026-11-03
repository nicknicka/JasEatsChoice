package com.xx.jaseatschoicejava.config;

/**
 * 统一解析 AI key：运行时文件优先，通用 AI key 兜底。
 */
public final class AiApiKeyResolver {

    private AiApiKeyResolver() {
    }

    public static String resolve(AIConfig aiConfig, AiRuntimeKeyStore runtimeKeyStore) {
        String runtimeKey = runtimeKeyStore == null ? "" : runtimeKeyStore.readKey();
        return resolve(aiConfig, runtimeKey);
    }

    public static String resolve(AIConfig aiConfig, String runtimeKey) {
        if (hasText(runtimeKey)) {
            return runtimeKey.trim();
        }
        String genericKey = aiConfig != null ? aiConfig.getApiKey() : null;
        return hasText(genericKey) ? genericKey.trim() : "";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
