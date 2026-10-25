package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 审核结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "审核结果DTO")
public class ReviewResultDTO {

    @ApiModelProperty(value = "审核通过数量")
    private Integer approvedCount;

    @ApiModelProperty(value = "审核驳回数量")
    private Integer rejectedCount;

    @ApiModelProperty(value = "失败原因列表")
    private List<String> failedReasons;

    public static ReviewResultDTO success(Integer approvedCount, Integer rejectedCount) {
        return new ReviewResultDTO(approvedCount, rejectedCount, null);
    }

    public static ReviewResultDTO failure(List<String> failedReasons) {
        return new ReviewResultDTO(0, 0, failedReasons);
    }
}
