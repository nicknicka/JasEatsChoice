package com.xx.jaseatschoicejava.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 饮食禁忌冲突检测结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "饮食禁忌冲突检测结果DTO")
public class AllergyCheckResultDTO {

    @ApiModelProperty(value = "是否有冲突")
    private Boolean hasConflict;

    @ApiModelProperty(value = "冲突详情列表")
    private List<ConflictItem> conflicts;

    /**
     * 冲突项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictItem {
        @ApiModelProperty(value = "菜品ID")
        private Long dishId;

        @ApiModelProperty(value = "菜品名称")
        private String dishName;

        @ApiModelProperty(value = "冲突用户列表")
        private List<UserConflict> conflictUsers;
    }

    /**
     * 用户冲突
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserConflict {
        @ApiModelProperty(value = "用户ID")
        private Long userId;

        @ApiModelProperty(value = "昵称")
        private String nickname;

        @ApiModelProperty(value = "过敏食材列表")
        private List<String> allergies;
    }
}
