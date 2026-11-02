package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端提交的公告 Embedding 候选配置。apiKey 留空表示沿用当前生效的 Embedding 运行时 key。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "公告 Embedding 候选配置")
public class AiEmbeddingCandidateDTO {

    @ApiModelProperty("Embedding 供应商")
    private String provider;

    @ApiModelProperty("Embedding 模型")
    private String model;

    @ApiModelProperty("向量维度")
    private Integer dimensions;

    @ApiModelProperty("服务地址")
    private String baseUrl;

    @ApiModelProperty("候选 Embedding key，留空沿用当前 key")
    private String apiKey;
}
