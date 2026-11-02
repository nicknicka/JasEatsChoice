package com.xx.jaseatschoicejava.config;

/**
 * 统一解析 AI key：运行时文件优先，供应商专属 key 次之，通用 AI key 兜底。
 */
public final class AiApiKeyResolver {

    private AiApiKeyResolver() {
    }

    public static String resolve(
            ZhipuAIConfig zhipuAIConfig,
            AIConfig aiConfig,
            AiRuntimeKeyStore runtimeKeyStore) {
        String runtimeKey = runtimeKeyStore == null ? "" : runtimeKeyStore.readKey();
        return resolve(zhipuAIConfig, aiConfig, runtimeKey);
    }

    /**
     * 运行时密钥槽位优先，其次供应商专属 key，最后通用 AI key。
     * Embedding 使用自己的运行时槽位，因此传自己的 key 进来即可。
     */
    public static String resolve(
            ZhipuAIConfig zhipuAIConfig,
            AIConfig aiConfig,
            String runtimeKey) {
        if (hasText(runtimeKey)) {
            return runtimeKey;
        }
        return resolve(zhipuAIConfig, aiConfig);
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
