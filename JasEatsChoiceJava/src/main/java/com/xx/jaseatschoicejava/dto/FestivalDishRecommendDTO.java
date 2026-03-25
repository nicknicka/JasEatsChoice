package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 节日推荐菜品DTO
 *
 * @author Claude
 * @since 2025-01-31
 */
@Data
public class FestivalDishRecommendDTO {

    /**
     * 节日ID
     */
    @NotBlank(message = "节日ID不能为空")
    private String festivalId;

    /**
     * 菜品ID列表
     */
    @NotNull(message = "菜品ID列表不能为空")
    private List<String> dishIds;

    /**
     * 推荐类型
     */
    @NotBlank(message = "推荐类型不能为空")
    private String recommendType;

    /**
     * 推荐理由
     */
    private String recommendReason;

    /**
     * 展示位置（0-首页, 1-列表顶, 2-banner）
     */
    private Integer position;

    /**
     * 优先级
     */
    private Integer priority;
}
