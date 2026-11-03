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
    private String embeddingProvider;
    private String embeddingModel;
    private int embeddingDimensions;
    private String embeddingBaseUrl;

    /** 是否已经用真实图片请求验证过视觉模型。 */
    private boolean visionVerified;

    /** 最近一次视觉验证失败原因；验证成功时为空。 */
    private String visionVerificationReason;

    /** 旧配置迁移后是否需要管理员重新填写。 */
    private boolean needsReconfiguration;

    /** 需要重新配置时展示给管理员的原因。 */
    private String configurationNotice;
}
