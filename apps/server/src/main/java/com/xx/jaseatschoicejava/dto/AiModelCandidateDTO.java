package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员提交的非密钥 AI 模型候选配置。
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "AI 模型候选配置")
public class AiModelCandidateDTO {

    @ApiModelProperty("AI 供应商")
    private String provider;

    @ApiModelProperty("聊天模型")
    private String model;

    @ApiModelProperty("Supervisor 模型")
    private String supervisorModel;

    @ApiModelProperty("视觉模型")
    private String visionModel;

    @ApiModelProperty("服务地址，空表示供应商默认")
    private String baseUrl;

    @ApiModelProperty("超时时间（毫秒）")
    private Integer timeoutMs;

    @ApiModelProperty("温度")
    private Double temperature;

    @ApiModelProperty("候选 API key；成功激活前不持久化，响应不回传")
    private String apiKey;

    @ApiModelProperty("公告 Embedding 候选配置；为空表示不改动")
    private AiEmbeddingCandidateDTO embedding;
}
