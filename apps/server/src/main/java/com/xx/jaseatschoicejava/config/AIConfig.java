package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 通用 AI 配置类
 *
 * 供应商无关的配置入口。本项目只使用 OpenAI-compatible 适配器，
 * 所有模型、地址和超时都从这里读取。
 *
 * @since 2026-10-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * 唯一支持的适配器。保留字段用于读取并迁移旧配置。
     */
    private String provider = "openai-compatible";

    /**
     * 通用 API Key。留空时应用仍可启动，AI 能力标记为未配置。
     */
    private String apiKey = "";

    /**
     * 通用文本推理模型名
     */
    private String model = "gpt-4o-mini";

    /**
     * Supervisor 专用模型名（更强推理）
     */
    private String supervisorModel = "gpt-4o-mini";

    /**
     * 视觉识别模型名
     */
    private String visionModel = "gpt-4o-mini";

    /**
     * OpenAI-compatible API 根地址；留空时 AI 能力标记为未配置。
     */
    private String baseUrl = "";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;

    /**
     * 温度参数
     */
    private Double temperature = 0.7;
}
