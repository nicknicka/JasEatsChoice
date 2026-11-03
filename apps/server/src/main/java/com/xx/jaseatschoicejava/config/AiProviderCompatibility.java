package com.xx.jaseatschoicejava.config;

/**
 * 历史 AI provider 配置的兼容入口。
 *
 * 项目运行时只注册 OpenAI-compatible 适配器；这里只负责把已持久化的旧 provider
 * 值转换为当前值，不注册、不构造任何旧供应商实现。
 */
public final class AiProviderCompatibility {

    public static final String PROVIDER_CODE = "openai-compatible";
    private static final String LEGACY_PROVIDER_CODE = "zhipu";

    private AiProviderCompatibility() {
    }

    public static String normalize(String provider) {
        if (provider == null || provider.isBlank()) {
            return provider;
        }
        String trimmed = provider.trim();
        return LEGACY_PROVIDER_CODE.equalsIgnoreCase(trimmed) ? PROVIDER_CODE : trimmed;
    }
}
