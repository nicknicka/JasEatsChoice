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

    /**
     * 食谱优化的统一超时时间（毫秒）。
     *
     * 模型调用、SSE 连接与前端超时提示共用这一份配置，避免四处各自写死。
     */
    private Integer recipeStreamTimeoutMs = 120000;

    /**
     * 环境变量显式声明「视觉模型已通过图片请求验证」。
     *
     * 默认 false：能力探测仍以真实行为为准，管理员在运行时配置里保存并激活时
     * 会用一张图片请求真实验证（见 AiRuntimeConfigServiceImpl#evaluateCandidate）。
     * 这里只给受控部署（本地联调、E2E 覆盖层指向仓库内假服务）一个显式开关，
     * 以免必须依赖一次在线探测才能开放视觉能力。
     */
    private boolean visionVerified = false;
}
