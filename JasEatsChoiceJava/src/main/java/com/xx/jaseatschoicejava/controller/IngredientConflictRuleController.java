package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.IngredientConflictRule;
import com.xx.jaseatschoicejava.service.IngredientConflictRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 食材冲突规则控制器
 *
 * @author Claude
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/v1/ingredient-conflict-rules")
public class IngredientConflictRuleController {

    @Autowired
    private IngredientConflictRuleService conflictRuleService;

    /**
     * 获取所有启用的冲突规则
     */
    @GetMapping
    public ResponseResult<?> getEnabledRules() {
        List<IngredientConflictRule> rules = conflictRuleService.getEnabledRules();
        return ResponseResult.success(rules);
    }

    /**
     * 根据冲突类型获取规则
     */
    @GetMapping("/type/{conflictType}")
    public ResponseResult<?> getRulesByType(@PathVariable String conflictType) {
        List<IngredientConflictRule> rules = conflictRuleService.getRulesByType(conflictType);
        return ResponseResult.success(rules);
    }

    /**
     * 根据严重程度获取规则
     */
    @GetMapping("/severity/{severity}")
    public ResponseResult<?> getRulesBySeverity(@PathVariable Integer severity) {
        List<IngredientConflictRule> rules = conflictRuleService.getRulesBySeverity(severity);
        return ResponseResult.success(rules);
    }

    /**
     * 检查菜品食材是否存在冲突
     */
    @PostMapping("/check-dish")
    public ResponseResult<?> checkDishConflicts(@RequestBody Map<String, Object> request) {
        String ingredientsJson = (String) request.get("ingredientsJson");
        if (ingredientsJson == null) {
            return ResponseResult.fail("400", "缺少食材JSON参数");
        }

        Map<String, Object> result = conflictRuleService.checkDishConflicts(ingredientsJson);
        return ResponseResult.success(result);
    }

    /**
     * 检查食材ID列表是否存在冲突
     */
    @PostMapping("/check-ingredients")
    public ResponseResult<?> checkConflicts(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ingredientIds = (List<Long>) request.get("ingredientIds");
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return ResponseResult.fail("400", "缺少食材ID列表参数");
        }

        List<IngredientConflictRule> conflicts = conflictRuleService.checkConflicts(ingredientIds);
        return ResponseResult.success(Map.of(
                "hasConflicts", !conflicts.isEmpty(),
                "conflicts", conflicts,
                "conflictCount", conflicts.size()
        ));
    }

    /**
     * 创建冲突规则（管理员功能）
     */
    @PostMapping
    public ResponseResult<?> createRule(@RequestBody IngredientConflictRule rule) {
        // 设置默认值
        if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }
        if (rule.getSeverity() == null) {
            rule.setSeverity(2); // 默认中等风险
        }
        if (rule.getPriority() == null) {
            rule.setPriority(3); // 默认低优先级
        }

        boolean saved = conflictRuleService.save(rule);
        if (saved) {
            return ResponseResult.success(rule);
        }
        return ResponseResult.fail("500", "创建冲突规则失败");
    }

    /**
     * 更新冲突规则（管理员功能）
     */
    @PutMapping("/{id}")
    public ResponseResult<?> updateRule(@PathVariable String id, @RequestBody IngredientConflictRule rule) {
        rule.setId(id);
        boolean updated = conflictRuleService.updateById(rule);
        if (updated) {
            return ResponseResult.success(conflictRuleService.getById(id));
        }
        return ResponseResult.fail("500", "更新冲突规则失败");
    }

    /**
     * 删除冲突规则（管理员功能）
     */
    @DeleteMapping("/{id}")
    public ResponseResult<?> deleteRule(@PathVariable String id) {
        boolean deleted = conflictRuleService.removeById(id);
        if (deleted) {
            return ResponseResult.success("删除冲突规则成功");
        }
        return ResponseResult.fail("500", "删除冲突规则失败");
    }

    /**
     * 批量删除冲突规则（管理员功能）
     */
    @DeleteMapping("/batch")
    public ResponseResult<?> batchDeleteRules(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) request.get("ids");

        if (ids == null || ids.isEmpty()) {
            return ResponseResult.fail("400", "请选择要删除的规则");
        }

        boolean deleted = conflictRuleService.removeByIds(ids);
        if (deleted) {
            return ResponseResult.success("批量删除冲突规则成功");
        }
        return ResponseResult.fail("500", "批量删除冲突规则失败");
    }
}
