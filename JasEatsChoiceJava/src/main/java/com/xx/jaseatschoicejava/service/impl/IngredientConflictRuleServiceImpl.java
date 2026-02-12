package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.IngredientConflictRule;
import com.xx.jaseatschoicejava.mapper.IngredientConflictRuleMapper;
import com.xx.jaseatschoicejava.service.IngredientConflictRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 食材冲突规则服务实现
 *
 * @author Claude
 * @since 2025-01-30
 */
@Slf4j
@Service
public class IngredientConflictRuleServiceImpl extends ServiceImpl<IngredientConflictRuleMapper, IngredientConflictRule>
        implements IngredientConflictRuleService {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<IngredientConflictRule> checkConflicts(List<Long> ingredientIds) {
        // 查询所有启用的冲突规则
        List<IngredientConflictRule> rules = getEnabledRules();
        List<IngredientConflictRule> conflicts = new ArrayList<>();

        // 检查每个规则是否匹配
        for (IngredientConflictRule rule : rules) {
            if (matchesRule(ingredientIds, rule)) {
                conflicts.add(rule);
            }
        }

        return conflicts;
    }

    @Override
    public Map<String, Object> checkDishConflicts(String ingredientsJson) {
        Map<String, Object> result = new HashMap<>();
        List<IngredientConflictRule> conflicts = new ArrayList<>();
        List<String> conflictTypes = new ArrayList<>();
        int maxSeverity = 0;

        try {
            // 解析食材JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> ingredientsMap = objectMapper.readValue(ingredientsJson, Map.class);

            // 收集所有食材名称
            Set<String> ingredientNames = new HashSet<>();

            // 处理必选食材
            Object required = ingredientsMap.get("requiredIngredients");
            if (required == null) {
                required = ingredientsMap.get("required");
            }
            if (required == null) {
                required = ingredientsMap.get("mandatory");
            }
            if (required instanceof List) {
                for (Object item : (List<?>) required) {
                    if (item != null) {
                        ingredientNames.add(item.toString());
                    }
                }
            }

            // 处理可选食材
            Object optional = ingredientsMap.get("optionalIngredients");
            if (optional == null) {
                optional = ingredientsMap.get("optional");
            }
            if (optional instanceof List) {
                for (Object item : (List<?>) optional) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> ingredientMap = (Map<String, Object>) item;
                        Object name = ingredientMap.get("name");
                        if (name != null) {
                            ingredientNames.add(name.toString());
                        }
                    } else if (item != null) {
                        ingredientNames.add(item.toString());
                    }
                }
            }

            // 检查冲突规则
            List<IngredientConflictRule> rules = getEnabledRules();
            for (IngredientConflictRule rule : rules) {
                if (matchesIngredientNames(ingredientNames, rule)) {
                    conflicts.add(rule);
                    String type = rule.getConflictType();
                    if (!conflictTypes.contains(type)) {
                        conflictTypes.add(type);
                    }
                    Integer severity = rule.getSeverity();
                    if (severity != null && severity > maxSeverity) {
                        maxSeverity = severity;
                    }
                }
            }

        } catch (Exception e) {
            log.error("解析食材JSON失败: {}", ingredientsJson, e);
        }

        result.put("hasConflicts", !conflicts.isEmpty());
        result.put("conflicts", conflicts);
        result.put("conflictTypes", conflictTypes);
        result.put("maxSeverity", maxSeverity);
        result.put("severityLevel", getSeverityLevel(maxSeverity));
        result.put("severityText", getSeverityText(maxSeverity));
        result.put("suggestion", generateSuggestion(conflicts));

        return result;
    }

    @Override
    public List<IngredientConflictRule> getEnabledRules() {
        return lambdaQuery()
                .eq(IngredientConflictRule::getEnabled, true)
                .orderByAsc(IngredientConflictRule::getPriority)
                .orderByDesc(IngredientConflictRule::getSeverity)
                .list();
    }

    @Override
    public List<IngredientConflictRule> getRulesByType(String conflictType) {
        return lambdaQuery()
                .eq(IngredientConflictRule::getConflictType, conflictType)
                .eq(IngredientConflictRule::getEnabled, true)
                .orderByDesc(IngredientConflictRule::getSeverity)
                .list();
    }

    @Override
    public List<IngredientConflictRule> getRulesBySeverity(Integer severity) {
        return lambdaQuery()
                .eq(IngredientConflictRule::getSeverity, severity)
                .eq(IngredientConflictRule::getEnabled, true)
                .orderByAsc(IngredientConflictRule::getPriority)
                .list();
    }

    /**
     * 判断食材ID列表是否匹配规则
     */
    private boolean matchesRule(List<Long> ingredientIds, IngredientConflictRule rule) {
        // 这里简化处理，实际应该检查食材ID是否在规则的主要食材列表中
        // 完整实现需要建立食材ID与食材名称的映射关系
        return false;
    }

    /**
     * 判断食材名称是否匹配规则
     */
    private boolean matchesIngredientNames(Set<String> ingredientNames, IngredientConflictRule rule) {
        try {
            @SuppressWarnings("unchecked")
            List<String> mainIngredients = objectMapper.readValue(rule.getMainIngredients(), List.class);

            // 检查是否包含任一主要食材
            for (String mainIngredient : mainIngredients) {
                if (ingredientNames.contains(mainIngredient)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("解析规则食材失败: {}", rule.getMainIngredients(), e);
            return false;
        }
    }

    /**
     * 获取严重程度级别
     */
    private String getSeverityLevel(int severity) {
        if (severity >= 3) {
            return "HIGH";
        } else if (severity >= 2) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 获取严重程度文本
     */
    private String getSeverityText(int severity) {
        if (severity >= 3) {
            return "高风险";
        } else if (severity >= 2) {
            return "中风险";
        } else {
            return "低风险";
        }
    }

    /**
     * 生成建议
     */
    private String generateSuggestion(List<IngredientConflictRule> conflicts) {
        if (conflicts.isEmpty()) {
            return "食材搭配安全";
        }

        StringBuilder suggestion = new StringBuilder();
        suggestion.append("检测到").append(conflicts.size()).append("个风险：");

        for (int i = 0; i < conflicts.size() && i < 3; i++) {
            if (i > 0) {
                suggestion.append("；");
            }
            IngredientConflictRule rule = conflicts.get(i);
            suggestion.append(rule.getConflictType());
            if (rule.getSuggestion() != null) {
                suggestion.append("(").append(rule.getSuggestion()).append(")");
            }
        }

        if (conflicts.size() > 3) {
            suggestion.append("等");
        }

        return suggestion.toString();
    }
}
