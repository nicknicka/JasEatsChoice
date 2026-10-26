package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 通用 AI 配置类
 *
 * 供应商无关的配置入口。具体供应商（智谱、通义、豆包等）的专属参数
 * 由各自的 Conditional 配置类读取；这里只放跨供应商通用的字段。
 *
 * 配置键：ai.provider 决定激活哪一家供应商实现，默认 zhipu。
 *
 * @since 2026-10-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * AI 供应商：zhipu / qwen / doubao / ...
     * 默认 zhipu，与历史行为一致。
     */
    private String provider = "zhipu";

    /**
     * 通用 API Key（供应商优先读自己的，没有再回退到这里）
     */
    private String apiKey = "";

    /**
     * 通用文本推理模型名
     */
    private String model = "glm-4-flash";

    /**
     * Supervisor 专用模型名（更强推理）
     */
    private String supervisorModel = "glm-4-flash";

    /**
     * 视觉识别模型名
     */
    private String visionModel = "glm-4.1v-thinking-flash";

    /**
     * API 基础 URL（可选，留空用供应商默认）
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
