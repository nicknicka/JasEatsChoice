package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 高德地图配置类
 */
@Component
@ConfigurationProperties(prefix = "amap")
public class AMapConfig {

    /**
     * API Key（用于前端 JavaScript API）
     * 这个 key 可以暴露给前端
     */
    private String apiKey;

    /**
     * 安全密钥（用于后端 Web 服务 API）
     * ⚠️ 这个密钥不能暴露给前端！
     */
    private String secretKey;

    /**
     * 是否启用后端代理模式
     * true: 所有地图 API 请求通过后端代理
     * false: 前端直接调用高德地图 API
     */
    private Boolean proxyEnabled = true;

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Boolean getProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(Boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }
}
