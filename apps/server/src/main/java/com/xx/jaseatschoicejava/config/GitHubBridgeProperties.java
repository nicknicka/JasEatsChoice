package com.xx.jaseatschoicejava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * GitHub Issue 桥接配置。所有值来自环境注入，仓库只允许占位符。
 */
@Configuration
public class GitHubBridgeProperties {

    @Value("${github.bridge.repo:}")
    private String repo;

    @Value("${github.bridge.token:}")
    private String token;

    @Value("${github.bridge.api-base:https://api.github.com}")
    private String apiBase;

    public String getRepo() {
        return repo;
    }

    public String getToken() {
        return token;
    }

    public String getApiBase() {
        return apiBase;
    }

    public boolean isConfigured() {
        return repo != null && !repo.isBlank() && token != null && !token.isBlank();
    }
}
