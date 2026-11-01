package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * provider 可用的单个模型选项。用于管理端模型下拉，不携带密钥。
 *
 * supportsImages 为 null 表示未知（例如静态预设），前端此时不据此过滤。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "AI 模型选项")
public class AiModelOption {

    @ApiModelProperty("模型 id（调用时传给 provider 的值）")
    private String id;

    @ApiModelProperty("展示名")
    private String name;

    @ApiModelProperty("是否支持图片输入")
    private Boolean supportsImages;
}
