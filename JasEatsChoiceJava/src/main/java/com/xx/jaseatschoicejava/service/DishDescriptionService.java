package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.DishDescriptionRequestDTO;

/**
 * 菜品描述生成服务接口
 */
public interface DishDescriptionService {

    /**
     * AI生成菜品描述
     * @param request 生成请求
     * @return 生成的描述
     */
    String generateDescription(DishDescriptionRequestDTO request);
}
