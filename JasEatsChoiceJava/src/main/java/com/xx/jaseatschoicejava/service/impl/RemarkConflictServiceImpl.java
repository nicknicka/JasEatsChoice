package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.RemarkConflictCheckDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.IngredientConflictRule;
import com.xx.jaseatschoicejava.enums.RemarkPriority;
import com.xx.jaseatschoicejava.enums.TasteTag;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.mapper.IngredientConflictRuleMapper;
import com.xx.jaseatschoicejava.service.RemarkConflictService;
import com.xx.jaseatschoicejava.vo.RemarkConflictCheckVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 备注冲突检测服务实现类
 *
 * @author Claude
 * @since 2025-01-30
 */
@Slf4j
@Service
public class RemarkConflictServiceImpl implements RemarkConflictService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private IngredientConflictRuleMapper conflictRuleMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RemarkConflictCheckVO checkConflict(RemarkConflictCheckDTO dto) {
        RemarkConflictCheckVO result = new RemarkConflictCheckVO();
        result.setHasConflict(false);
        result.setConflicts(new ArrayList<>());
        result.setSuggestions(new ArrayList<>());

        try {
            // 1. 获取菜品信息
            Dish dish = dishMapper.selectById(dto.getDishId());
            if (dish == null) {
                log.warn("菜品不存在：dishId={}", dto.getDishId());
                return result;
            }

            // 2. 解析备注中的标签
            List<String> remarkTags = parseTasteTags(dto.getRemark());
            if (dto.getTasteTags() != null && !dto.getTasteTags().isEmpty()) {
                remarkTags.addAll(dto.getTasteTags());
            }

            // 3. 获取所有冲突规则
            List<IngredientConflictRule> rules = conflictRuleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IngredientConflictRule>()
                    .eq(IngredientConflictRule::getEnabled, true)
            );

            // 4. 解析菜品食材
            Set<String> dishIngredients = parseIngredients(dish.getIngredients());

            // 5. 检测冲突
            List<RemarkConflictCheckVO.ConflictItem> conflicts = new ArrayList<>();
            List<String> suggestions = new ArrayList<>();

            for (IngredientConflictRule rule : rules) {
                // 检测食材冲突
                Set<String> ruleIngredients = parseIngredients(rule.getMainIngredients());
                Set<String> conflictIngredients = new HashSet<>(dishIngredients);
                conflictIngredients.retainAll(ruleIngredients);

                if (!conflictIngredients.isEmpty()) {
                    // 检测标签冲突
                    List<String> ruleConflictTags = parseJsonArray(rule.getConflictTags());
                    for (String tag : remarkTags) {
                        if (ruleConflictTags.contains(tag)) {
                            RemarkConflictCheckVO.ConflictItem conflict = new RemarkConflictCheckVO.ConflictItem();
                            conflict.setConflictType(rule.getConflictType());
                            conflict.setDescription(rule.getDescription());
                            conflict.setConflictItem(String.join("、", conflictIngredients));
                            conflict.setSeverity(rule.getSeverity());
                            conflict.setPriority(rule.getPriority());
                            conflict.setColor(getPriorityColor(rule.getPriority()));

                            conflicts.add(conflict);
                            result.setHasConflict(true);

                            // 添加建议
                            if (rule.getSuggestion() != null && !rule.getSuggestion().isEmpty()) {
                                suggestions.add(rule.getSuggestion());
                            }
                        }
                    }
                }
            }

            // 6. 检测用户过敏食材
            if (dto.getUserAllergies() != null && !dto.getUserAllergies().isEmpty()) {
                Set<String> userAllergies = parseIngredients(dto.getUserAllergies());
                Set<String> allergyConflict = new HashSet<>(dishIngredients);
                allergyConflict.retainAll(userAllergies);

                if (!allergyConflict.isEmpty()) {
                    RemarkConflictCheckVO.ConflictItem conflict = new RemarkConflictCheckVO.ConflictItem();
                    conflict.setConflictType("ALLERGY");
                    conflict.setDescription("该菜品包含您的过敏食材");
                    conflict.setConflictItem(String.join("、", allergyConflict));
                    conflict.setSeverity(3);
                    conflict.setPriority(RemarkPriority.HIGH.getSort());
                    conflict.setColor(RemarkPriority.HIGH.getColor());

                    conflicts.add(conflict);
                    result.setHasConflict(true);
                    suggestions.add("建议选择其他菜品或询问商家是否可以替换过敏食材");
                }
            }

            result.setConflicts(conflicts);
            result.setSuggestions(suggestions);

            // 7. 设置冲突级别
            if (result.getHasConflict()) {
                int maxPriority = conflicts.stream()
                    .mapToInt(RemarkConflictCheckVO.ConflictItem::getPriority)
                    .min()
                    .orElse(4);
                result.setConflictLevel(getConflictLevel(maxPriority));
            }

        } catch (Exception e) {
            log.error("备注冲突检测失败", e);
        }

        return result;
    }

    @Override
    public List<String> parseTasteTags(String remark) {
        if (remark == null || remark.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> tags = new ArrayList<>();

        // 匹配所有口味标签枚举
        for (TasteTag tasteTag : TasteTag.values()) {
            // 使用正则表达式匹配标签名称
            Pattern pattern = Pattern.compile(tasteTag.getName());
            Matcher matcher = pattern.matcher(remark);
            if (matcher.find()) {
                tags.add(tasteTag.getCode());
            }
        }

        return tags;
    }

    @Override
    public List<String> getRecommendedTasteTags(String dishId) {
        // 返回常用的口味标签
        return Arrays.asList(
            TasteTag.MILD_NO_SPICY.getCode(),
            TasteTag.NO_ONION.getCode(),
            TasteTag.NO_GARLIC.getCode(),
            TasteTag.NO_CORIANDER.getCode(),
            TasteTag.LESS_SUGAR.getCode(),
            TasteTag.LESS_SALT.getCode()
        );
    }

    @Override
    public String formatRemark(String originalRemark, List<String> tasteTags) {
        if (tasteTags == null || tasteTags.isEmpty()) {
            return originalRemark;
        }

        // 构建标签前缀
        StringBuilder formattedRemark = new StringBuilder();

        // 按类别分组标签
        Map<String, List<TasteTag>> tagsByCategory = tasteTags.stream()
            .map(TasteTag::getByCode)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(TasteTag::getCategory));

        // 格式化标签
        for (Map.Entry<String, List<TasteTag>> entry : tagsByCategory.entrySet()) {
            List<String> tagNames = entry.getValue().stream()
                .map(TasteTag::getName)
                .collect(Collectors.toList());
            formattedRemark.append("【").append(String.join("、", tagNames)).append("】");
        }

        // 添加原始备注
        if (originalRemark != null && !originalRemark.trim().isEmpty()) {
            formattedRemark.append(" ").append(originalRemark);
        }

        return formattedRemark.toString().trim();
    }

    /**
     * 解析食材JSON字符串
     */
    private Set<String> parseIngredients(String ingredientsJson) {
        Set<String> ingredients = new HashSet<>();
        if (ingredientsJson == null || ingredientsJson.trim().isEmpty()) {
            return ingredients;
        }

        try {
            if (ingredientsJson.startsWith("[")) {
                // JSON数组格式
                List<String> ingredientList = objectMapper.readValue(ingredientsJson, new TypeReference<List<String>>() {});
                ingredients.addAll(ingredientList);
            } else {
                // 简单字符串，按逗号分隔
                String[] parts = ingredientsJson.split("[,，、]");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        ingredients.add(part.trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析食材JSON失败：{}", ingredientsJson, e);
        }

        return ingredients;
    }

    /**
     * 解析JSON数组
     */
    private List<String> parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析JSON数组失败：{}", jsonArray, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取优先级颜色
     */
    private String getPriorityColor(Integer priority) {
        switch (priority) {
            case 1:
                return RemarkPriority.HIGH.getColor();
            case 2:
                return RemarkPriority.MEDIUM_HIGH.getColor();
            case 3:
                return RemarkPriority.MEDIUM.getColor();
            default:
                return RemarkPriority.LOW.getColor();
        }
    }

    /**
     * 获取冲突级别
     */
    private String getConflictLevel(Integer priority) {
        switch (priority) {
            case 1:
                return "HIGH";
            case 2:
                return "MEDIUM_HIGH";
            case 3:
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
}
