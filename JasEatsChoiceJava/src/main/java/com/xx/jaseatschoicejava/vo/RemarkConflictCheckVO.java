package com.xx.jaseatschoicejava.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 备注冲突检测结果VO
 *
 * @author Claude
 * @since 2025-01-30
 */
@Data
@ApiModel(description = "备注冲突检测结果")
public class RemarkConflictCheckVO {

    @ApiModelProperty(value = "是否存在冲突")
    private Boolean hasConflict;

    @ApiModelProperty(value = "冲突级别：HIGH-高（过敏）, MEDIUM_HIGH-中高（核心需求）, MEDIUM-中（口味）, LOW-低（一般）")
    private String conflictLevel;

    @ApiModelProperty(value = "冲突列表")
    private List<ConflictItem> conflicts;

    @ApiModelProperty(value = "建议的替代方案")
    private List<String> suggestions;

    @Data
    public static class ConflictItem {
        @ApiModelProperty(value = "冲突类型：ALLERGY-过敏, INCOMPATIBLE-食材冲突, TAG_MISMATCH-标签冲突")
        private String conflictType;

        @ApiModelProperty(value = "冲突描述")
        private String description;

        @ApiModelProperty(value = "冲突的食材/标签")
        private String conflictItem;

        @ApiModelProperty(value = "严重程度：1-低, 2-中, 3-高")
        private Integer severity;

        @ApiModelProperty(value = "推荐优先级")
        private Integer priority;

        @ApiModelProperty(value = "显示颜色")
        private String color;
    }

    @Data
    public static class RemarkSuggestion {
        @ApiModelProperty(value = "建议内容")
        private String content;

        @ApiModelProperty(value = "建议类型")
        private String type;
    }
}
