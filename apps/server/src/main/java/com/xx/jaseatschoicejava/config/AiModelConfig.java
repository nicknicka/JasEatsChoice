package com.xx.jaseatschoicejava.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 运行时使用的非密钥模型配置。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AiModelConfig {
    private String provider;
    private String model;
    private String supervisorModel;
    private String visionModel;
    private String baseUrl;
    private int timeoutMs;
    private double temperature;
}
