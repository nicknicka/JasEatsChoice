package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 想吃列表申诉DTO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "想吃列表申诉请求")
public class WishListAppealDTO {

    @ApiModelProperty(value = "想吃列表项ID", required = true)
    @NotBlank(message = "列表项ID不能为空")
    private String wishListItemId;

    @ApiModelProperty(value = "申诉内容", required = true)
    @NotBlank(message = "申诉内容不能为空")
    private String appealContent;
}
