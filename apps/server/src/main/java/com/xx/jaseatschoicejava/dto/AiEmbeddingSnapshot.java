package com.xx.jaseatschoicejava.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端只读的公告 Embedding 生效配置。该对象不携带 API key 明文。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "公告 Embedding 生效配置")
public class AiEmbeddingSnapshot {

    @ApiModelProperty("当前 Embedding 供应商")
    private String provider;

    @ApiModelProperty("当前 Embedding 模型")
    private String model;

    @ApiModelProperty("当前向量维度")
    private int dimensions;

    @ApiModelProperty("当前服务地址")
    private String baseUrl;

    @ApiModelProperty("后端已实现的 Embedding provider 列表")
    private List<String> providers;

    @ApiModelProperty("公告 RAG 状态：READY / REBUILDING / UNAVAILABLE / NOT_CONFIGURED / DISABLED")
    private String state;

    @ApiModelProperty("状态原因，失败时为失败原因")
    private String statusReason;

    @ApiModelProperty("Embedding key 脱敏状态")
    private AiRuntimeConfigSnapshot.AiKeyStatus keyStatus;
}
