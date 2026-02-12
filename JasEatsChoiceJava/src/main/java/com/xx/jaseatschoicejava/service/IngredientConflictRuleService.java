package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.IngredientConflictRule;

import java.util.List;
import java.util.Map;

/**
 * 食材冲突规则服务接口
 *
 * @author Claude
 * @since 2025-01-30
 */
public interface IngredientConflictRuleService extends IService<IngredientConflictRule> {

    /**
     * 检查食材组合是否存在冲突
     *
     * @param ingredientIds 食材ID列表
     * @return 冲突规则列表
     */
    List<IngredientConflictRule> checkConflicts(List<Long> ingredientIds);

    /**
     * 检查菜品食材是否存在冲突
     *
     * @param ingredientsJson 食材JSON字符串
     * @return 冲突检测结果
     */
    Map<String, Object> checkDishConflicts(String ingredientsJson);

    /**
     * 获取启用的冲突规则列表
     *
     * @return 规则列表
     */
    List<IngredientConflictRule> getEnabledRules();

    /**
     * 根据冲突类型获取规则
     *
     * @param conflictType 冲突类型
     * @return 规则列表
     */
    List<IngredientConflictRule> getRulesByType(String conflictType);

    /**
     * 根据严重程度获取规则
     *
     * @param severity 严重程度
     * @return 规则列表
     */
    List<IngredientConflictRule> getRulesBySeverity(Integer severity);
}
