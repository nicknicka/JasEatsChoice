package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 想吃列表审核DTO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "想吃列表审核请求")
public class WishListAuditDTO {

    @ApiModelProperty(value = "想吃列表项ID", required = true)
    @NotNull(message = "列表项ID不能为空")
    private String wishListItemId;

    @ApiModelProperty(value = "审核结果：true-通过, false-拒绝", required = true)
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @ApiModelProperty(value = "拒绝原因代码（拒绝时必填）")
    private Integer rejectionReasonCode;

    @ApiModelProperty(value = "拒绝原因说明（拒绝时必填）")
    private String rejectionReason;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "预计上架时间（审核通过时可填）")
    private String actualAvailableTime;
}
