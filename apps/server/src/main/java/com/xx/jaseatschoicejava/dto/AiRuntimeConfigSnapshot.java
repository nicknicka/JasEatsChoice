package com.xx.jaseatschoicejava.dto;

import java.util.List;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端只读 AI 运行时配置快照。该对象不携带 API key 明文。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "管理员 AI 运行时配置快照")
public class AiRuntimeConfigSnapshot {

    @ApiModelProperty("AI 供应商")
    private String provider;

    @ApiModelProperty("当前聊天模型")
    private String model;

    @ApiModelProperty("当前 Supervisor 模型")
    private String supervisorModel;

    @ApiModelProperty("当前视觉模型")
    private String visionModel;

    @ApiModelProperty("当前服务地址")
    private String baseUrl;

    @ApiModelProperty("当前超时时间（毫秒）")
    private int timeoutMs;

    @ApiModelProperty("当前温度")
    private Double temperature;

    @ApiModelProperty("当前快照加载时间")
    private String updatedAt;

    @ApiModelProperty("当前生效配置版本")
    private Long version;

    @ApiModelProperty("key 脱敏状态")
    private AiKeyStatus keyStatus;

    @ApiModelProperty("当前供应商能力状态")
    private AiCapabilitiesDTO capabilities;

    @ApiModelProperty("当前配置可用的模型列表")
    private List<String> supportedModels;

    @ApiModelProperty("后端已实现的 provider 列表")
    private List<AiProviderOption> providers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiKeyStatus {
        private boolean configured;
        private String fingerprint;
        private String last4;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiProviderOption {
        private String code;
        private String name;
        private List<String> models;
    }
}
