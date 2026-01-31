package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 食材冲突规则实体
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@TableName("t_ingredient_conflict_rule")
@ApiModel(description = "食材冲突规则")
public class IngredientConflictRule {

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "规则ID")
    private String id;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "冲突类型：ALLERGY-过敏, INCOMPATIBLE-食材冲突, CUISINE-烹饪禁忌")
    private String conflictType;

    @ApiModelProperty(value = "主要食材（JSON数组，如[\"辣椒\", \"辣椒粉\"]）")
    private String mainIngredients;

    @ApiModelProperty(value = "冲突标签（JSON数组，如[\"mild_no_spicy\", \"no_spicy\"]）")
    private String conflictTags;

    @ApiModelProperty(value = "严重程度：1-低, 2-中, 3-高")
    private Integer severity;

    @ApiModelProperty(value = "推荐优先级：1-高（红色）, 2-中高（黄色）, 3-中（蓝色）, 4-低（灰色）")
    private Integer priority;

    @ApiModelProperty(value = "冲突描述")
    private String description;

    @ApiModelProperty(value = "建议内容")
    private String suggestion;

    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
