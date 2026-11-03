package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 发布提取结果为食谱的请求参数。
 */
@Data
public class PublishRecipeDTO {

    /**
     * 餐次：breakfast/lunch/dinner/snack，缺省为 lunch。
     */
    private String mealType;
}
