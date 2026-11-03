package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上一版有效 AI 配置摘要。只包含非密钥配置和 key 状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "上一版 AI 配置摘要")
public class AiPreviousConfigSummary {

    @ApiModelProperty("上一版配置版本")
    private Long version;

    @ApiModelProperty("上一版生效时间")
    private String updatedAt;

    @ApiModelProperty("AI 供应商")
    private String provider;

    @ApiModelProperty("聊天模型")
    private String model;

    @ApiModelProperty("Supervisor 模型")
    private String supervisorModel;

    @ApiModelProperty("视觉模型")
    private String visionModel;

    @ApiModelProperty("服务地址")
    private String baseUrl;

    @ApiModelProperty("超时时间（毫秒）")
    private Integer timeoutMs;

    @ApiModelProperty("温度")
    private Double temperature;

    @ApiModelProperty("视觉模型是否已通过真实图片请求验证")
    private boolean visionVerified;

    @ApiModelProperty("视觉模型验证失败原因")
    private String visionVerificationReason;

    @ApiModelProperty("旧配置是否需要管理员重新填写")
    private boolean needsReconfiguration;

    @ApiModelProperty("需要重新配置时的原因")
    private String configurationNotice;

    @ApiModelProperty("key 是否仍配置")
    private boolean keyConfigured;

    @ApiModelProperty("key 不可逆指纹")
    private String keyFingerprint;
}
