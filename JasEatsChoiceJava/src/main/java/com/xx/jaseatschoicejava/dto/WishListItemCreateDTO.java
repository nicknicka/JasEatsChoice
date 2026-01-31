package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 想吃列表项创建DTO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "想吃列表项创建请求")
public class WishListItemCreateDTO {

    @ApiModelProperty(value = "商家ID（可选）", required = false)
    private String merchantId;

    @ApiModelProperty(value = "菜品名称", required = true)
    @NotBlank(message = "菜品名称不能为空")
    private String dishName;

    @ApiModelProperty(value = "菜品图片URL")
    private String dishImage;

    @ApiModelProperty(value = "口味要求")
    private String tasteRequirement;

    @ApiModelProperty(value = "详细描述")
    private String description;

    @ApiModelProperty(value = "参考食谱ID")
    private String recipeId;

    @ApiModelProperty(value = "期望上架时间")
    private LocalDateTime expectedAvailableTime;
}
