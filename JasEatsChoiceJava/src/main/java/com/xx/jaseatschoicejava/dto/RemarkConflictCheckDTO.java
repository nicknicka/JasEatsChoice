package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 备注冲突检测请求DTO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "备注冲突检测请求")
public class RemarkConflictCheckDTO {

    @ApiModelProperty(value = "菜品ID", required = true)
    @NotBlank(message = "菜品ID不能为空")
    private String dishId;

    @ApiModelProperty(value = "备注内容", required = true)
    private String remark;

    @ApiModelProperty(value = "选中的口味标签列表")
    private List<String> tasteTags;

    @ApiModelProperty(value = "用户过敏食材列表（JSON格式）")
    private String userAllergies;

    @ApiModelProperty(value = "用户饮食偏好标签")
    private List<String> preferenceTags;
}
