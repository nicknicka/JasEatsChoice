package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 申诉回复DTO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "申诉回复请求")
public class AppealReplyDTO {

    @ApiModelProperty(value = "想吃列表项ID", required = true)
    @NotBlank(message = "列表项ID不能为空")
    private String wishListItemId;

    @ApiModelProperty(value = "回复结果：true-同意申诉, false-拒绝申诉", required = true)
    private Boolean approved;

    @ApiModelProperty(value = "回复内容", required = true)
    @NotBlank(message = "回复内容不能为空")
    private String appealReply;
}
