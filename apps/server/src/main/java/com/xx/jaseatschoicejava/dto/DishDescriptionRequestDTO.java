package com.xx.jaseatschoicejava.dto;

import lombok.Data;
import java.util.List;

/**
 * 菜品描述生成请求DTO
 */
@Data
public class DishDescriptionRequestDTO {

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 主要食材
     */
    private List<String> ingredients;

    /**
     * 菜品分类
     */
    private String category;

    /**
     * 描述风格：traditional/health/story/promotion
     */
    private String style;
}
