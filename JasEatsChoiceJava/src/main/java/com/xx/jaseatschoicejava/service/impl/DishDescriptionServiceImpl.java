package com.xx.jaseatschoicejava.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.xx.jaseatschoicejava.dto.DishDescriptionRequestDTO;
import com.xx.jaseatschoicejava.service.DishDescriptionService;

import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;

/**
 * 菜品描述生成服务实现
 */
@Slf4j
@Service
public class DishDescriptionServiceImpl implements DishDescriptionService {

    private final ChatModel agentModel;

    public DishDescriptionServiceImpl(@Qualifier("aiModel") ChatModel aiModel) {
        this.agentModel = aiModel;
    }

    @Override
    public String generateDescription(DishDescriptionRequestDTO request) {
        try {
            String prompt = buildPrompt(request);
            String description = agentModel.chat(prompt);
            return description.trim();
        } catch (Exception e) {
            log.error("生成菜品描述失败", e);
            return getDefaultDescription(request);
        }
    }

    private String buildPrompt(DishDescriptionRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的餐饮文案撰写师，请为以下菜品生成一段吸引人的描述。\n\n");
        sb.append("菜品信息：\n");
        sb.append(String.format("- 菜品名称：%s\n", request.getName()));

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            sb.append(String.format("- 主要食材：%s\n", String.join("、", request.getIngredients())));
        }

        if (request.getCategory() != null) {
            sb.append(String.format("- 菜品分类：%s\n", request.getCategory()));
        }

        sb.append("\n描述风格要求：\n");

        String styleDesc = switch (request.getStyle()) {
            case "health" -> """
                【营养健康风格】
                - 强调菜品的营养价值和健康特点
                - 突出低脂、高蛋白、维生素等营养元素
                - 适合注重健康饮食的顾客
                """;
            case "story" -> """
                【情感故事风格】
                - 讲述菜品背后的故事或文化内涵
                - 营造温馨、有温度的氛围
                - 让顾客产生情感共鸣
                """;
            case "promotion" -> """
                【促销吸引风格】
                - 使用吸引眼球的促销语言
                - 强调性价比和限时优惠
                - 激发顾客的购买欲望
                """;
            default -> """
                【传统描述风格】
                - 突出菜品的色香味俱全
                - 描述口感和特色
                - 使用传统美食描述语言
                """;
        };

        sb.append(styleDesc);
        sb.append("\n要求：\n");
        sb.append("1. 描述控制在100字以内\n");
        sb.append("2. 语言生动有感染力\n");
        sb.append("3. 直接返回描述内容，不要其他解释\n");

        return sb.toString();
    }

    private String getDefaultDescription(DishDescriptionRequestDTO request) {
        String name = request.getName();
        String ingredients = request.getIngredients() != null && !request.getIngredients().isEmpty()
                ? String.join("、", request.getIngredients())
                : "精选食材";

        return switch (request.getStyle()) {
            case "health" -> String.format(
                    "【%s】富含优质蛋白和多种维生素，低脂健康，营养均衡。选用新鲜%s，采用健康烹饪方式，保留食材原味与营养。适合注重健康饮食的您，美味与健康兼得。",
                    name, ingredients
            );
            case "story" -> String.format(
                    "每一道【%s】都承载着厨师的匠心与故事。精选%s，经过多道工序精心烹制，只为给您带来最纯正的味觉体验。这不仅是一道菜，更是一份用心的传递，期待您的品尝。",
                    name, ingredients
            );
            case "promotion" -> String.format(
                    "🔥 限时特惠！【%s】精选%s，大师级烹饪，美味不容错过！数量有限，先到先得！立即下单，享受超值优惠！",
                    name, ingredients
            );
            default -> String.format(
                    "【%s】精选%s精心烹制，传承经典做法，色香味俱全。菜品口感鲜美，回味无穷，是您不可错过的美味佳肴。每一口都能品尝到食材的鲜美与厨师的匠心。",
                    name, ingredients
            );
        };
    }
}
