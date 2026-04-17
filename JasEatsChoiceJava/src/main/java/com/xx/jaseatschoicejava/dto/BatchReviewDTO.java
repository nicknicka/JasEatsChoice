package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 批量审核DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "批量审核DTO")
public class BatchReviewDTO {

    @ApiModelProperty(value = "加菜请求ID列表", required = true)
    private List<String> requestIds;

    @ApiModelProperty(value = "操作类型: approve-通过, reject-驳回", required = true)
    private String action;

    @ApiModelProperty(value = "驳回原因(驳回时必填)")
    private String rejectReason;

    @ApiModelProperty(value = "审核人ID", required = true)
    private String reviewerId;
}
