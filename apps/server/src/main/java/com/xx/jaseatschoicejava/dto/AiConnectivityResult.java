package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前 AI 配置连通性测试结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "AI 连通性测试结果")
public class AiConnectivityResult {

    @ApiModelProperty("是否连通")
    private boolean success;

    @ApiModelProperty("结果说明")
    private String message;

    @ApiModelProperty("测试时间")
    private String testedAt;

    @ApiModelProperty("测试使用的模型")
    private String model;
}
