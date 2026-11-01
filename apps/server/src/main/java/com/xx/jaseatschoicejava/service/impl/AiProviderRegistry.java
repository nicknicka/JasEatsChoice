package com.xx.jaseatschoicejava.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.dto.AiRuntimeConfigSnapshot;

/**
 * 已实现的 AI provider 适配器注册表。运行时配置服务不判断具体 provider。
 */
@Component
public class AiProviderRegistry {

    private final Map<String, AiModelClientFactory> providers = new LinkedHashMap<>();

    public AiProviderRegistry(List<AiModelClientFactory> factories) {
        if (factories == null) {
            return;
        }
        for (AiModelClientFactory factory : factories) {
            if (providers.putIfAbsent(factory.providerCode(), factory) != null) {
                throw new IllegalStateException("重复注册 AI provider：" + factory.providerCode());
            }
        }
    }

    public AiModelClientFactory required(String provider) {
        AiModelClientFactory factory = providers.get(provider);
        if (factory == null) {
            throw new IllegalArgumentException("暂不支持的 AI provider");
        }
        return factory;
    }

    public List<AiRuntimeConfigSnapshot.AiProviderOption> options() {
        return providers.values().stream()
            .map(factory -> new AiRuntimeConfigSnapshot.AiProviderOption(
                factory.providerCode(), factory.providerDisplayName(), factory.knownModels()))
            .toList();
    }
}
