package com.xx.jaseatschoicejava.agent.tools.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.agent.annotation.CardType;
import com.xx.jaseatschoicejava.agent.dto.UniCardDTO;
import com.xx.jaseatschoicejava.agent.util.UniCardBuilder;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下单引导工具类
 *
 * 为Agent提供智能下单引导功能，包括商家推荐、菜品预选、下单卡片生成等
 * 使用 UniCardBuilder 构建统一卡片格式
 *
 * @author Claude
 * @since 2026-03-25
 */
@Slf4j
@Service
public class OrderGuideTools {

    @Resource
    private MerchantService merchantService;

    @Resource
    private DishService dishService;

    /**
     * 推荐商家并生成下单卡片
     *
     * 根据用户需求推荐商家，AI自动预选菜品，使用 UniCardBuilder 生成统一卡片数据
     *
     * @param merchantId 商家ID（可选，如果不指定则自动推荐）
     * @param diningMode 就餐方式（dine_in=堂食 或 takeout=自取）
     * @param preference 用户偏好（可选，如"辣"、"清淡"等）
     * @return 商家下单卡片数据（UniCard JSON格式）
     */
    @Tool("""
        推荐商家并生成下单卡片

        **功能说明：**
        - 根据用户需求智能推荐商家
        - AI自动预选符合用户口味的菜品
        - 生成统一卡片数据供前端展示
        - 支持堂食和自取两种模式

        **何时使用：**
        - 用户说"我想买xx家的东西"
        - 用户说"我想吃xxx"
        - 用户询问"有什么推荐的"
        - 用户准备下单

        **参数：**
        - merchantId - 商家ID（可选，不指定则自动推荐）
        - diningMode - 就餐方式：dine_in（堂食）或 takeout（自取）
        - preference - 用户偏好，如"辣"、"清淡"、"营养均衡"（可选）

        **无需参数**，userId自动从上下文获取

        **返回：** 商家推荐卡片（UniCard JSON格式），包含：
        - 商家信息（ID、名称、评分、地址等）
        - AI预选的菜品列表（菜品ID、名称、价格、数量、推荐理由）
        - 就餐方式
        - 预估总价
        - 操作按钮配置

        **前端使用说明：**
        1. 解析返回的UniCard JSON数据
        2. 根据 schema 识别卡片版本
        3. 根据 element.tag 动态渲染对应组件
        4. 用户可以修改/增加菜品
        5. 用户确认后调用 createOrder 工具创建订单
        """)
    @CardType("merchant_order_card")
    public String recommendMerchantForOrder(
        AgenticScope scope,
        @P(value = "商家ID（可选，不指定则自动推荐）", required = false) String merchantId,
        @P("就餐方式（dine_in=堂食 或 takeout=自取）") String diningMode,
        @P(value = "用户偏好，如：辣、清淡、营养均衡", required = false) String preference
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return buildErrorText("无法获取用户信息，请重新登录");
        }
        log.info("[Tool] 推荐商家并生成下单卡片，userId: {}, merchantId: {}, diningMode: {}",
            userId, merchantId, diningMode);

        try {
            // 1. 验证就餐方式
            if (!diningMode.matches("^(dine_in|takeout)$")) {
                return buildErrorText("就餐方式错误，必须是：dine_in（堂食）或 takeout（自取）");
            }

            // 2. 获取商家信息
            Merchant merchant = resolveMerchant(merchantId);
            if (merchant == null) {
                return buildErrorText(merchantId != null ? "商家不存在，商家ID：" + merchantId : "暂无营业中的商家");
            }

            // 3. 获取商家菜品（只取在线的）
            List<Dish> allDishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getMerchantId, merchant.getId())
                    .eq(Dish::getIsOnline, true)
            );

            if (allDishes.isEmpty()) {
                return buildErrorText("该商家暂无上架菜品");
            }

            // 4. AI预选菜品
            List<PreSelectResult> preSelected = preSelectDishes(allDishes, preference);

            // 5. 计算预估总价
            BigDecimal estimatedTotal = calculateEstimatedTotal(preSelected, diningMode);

            // 6. 构建人类可读文本
            String humanText = buildHumanReadableText(merchant, preSelected, diningMode, estimatedTotal, preference);

            // 7. 使用 UniCardBuilder 构建卡片
            String cardJson = buildMerchantOrderCard(merchant, preSelected, diningMode, estimatedTotal, preference);

            log.info("[Tool] 推荐商家并生成下单卡片成功，merchantId: {}, 预选菜品数: {}",
                merchant.getId(), preSelected.size());

            return humanText + "\n\n" + cardJson;

        } catch (Exception e) {
            log.error("[Tool] 推荐商家并生成下单卡片失败", e);
            return buildErrorText("推荐失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户选择的菜品
     *
     * 用户在前端弹窗中修改菜品后，调用此方法更新选择
     *
     * @param merchantId 商家ID
     * @param selectedDishesJson 用户选择的菜品JSON
     * @param diningMode 就餐方式（dine_in=堂食 或 takeout=自取）
     * @return 更新后的订单信息
     */
    @Tool("""
        更新用户选择的菜品

        **功能说明：**
        - 用户在前端弹窗中修改菜品后调用
        - 重新计算价格
        - 更新订单预览

        **何时使用：**
        - 用户在弹窗中修改了菜品数量
        - 用户添加/删除了菜品
        - 用户点击"调整菜品"按钮

        **参数：**
        - merchantId - 商家ID
        - selectedDishesJson - 用户选择的菜品（JSON格式）
              格式：[{"dishId":"D001","quantity":2},{"dishId":"D002","quantity":1}]
        - diningMode - 就餐方式（dine_in=堂食 或 takeout=自取）

        **无需参数**，userId自动从上下文获取

        **返回：** 更新后的订单信息和价格明细
        """)
    public String updateSelectedDishes(
        AgenticScope scope,
        @P("商家ID") String merchantId,
        @P("用户选择的菜品（JSON格式）") String selectedDishesJson,
        @P("就餐方式（dine_in=堂食 或 takeout=自取）") String diningMode
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return buildErrorText("无法获取用户信息，请重新登录");
        }
        log.info("[Tool] 更新用户选择的菜品，userId: {}, merchantId: {}", userId, merchantId);

        try {
            // 解析并验证菜品列表
            List<ParsedDishItem> parsedItems = parseSelectedDishes(selectedDishesJson);
            if (parsedItems.isEmpty()) {
                return buildErrorText("请至少选择一道菜品");
            }

            // 查询菜品详情并构建列表
            List<UniCardDTO.DishItem> dishItems = new ArrayList<>();
            BigDecimal dishTotal = BigDecimal.ZERO;
            int totalItems = 0;

            for (ParsedDishItem parsed : parsedItems) {
                Dish dish = dishService.getById(parsed.dishId);
                if (dish == null) {
                    continue;
                }

                BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(parsed.quantity));
                dishTotal = dishTotal.add(subtotal);
                totalItems += parsed.quantity;

                dishItems.add(UniCardBuilder.createDishItem(
                    dish.getId(), dish.getName(), dish.getPrice(),
                    dish.getAvgRating() != null ? dish.getAvgRating().doubleValue() : null,
                    dish.getImage(), dish.getDescription(), dish.getCategory(),
                    null
                ));
            }

            // 计算其他费用
            boolean isTakeout = "takeout".equalsIgnoreCase(diningMode);
            BigDecimal packagingFee = isTakeout
                ? BigDecimal.valueOf(totalItems * 2.0)
                : BigDecimal.ZERO;
            BigDecimal totalAmount = dishTotal.add(packagingFee);

            // 构建人类可读文本
            String modeText = isTakeout ? "自取" : "堂食";
            String humanText = buildOrderSummaryText(dishItems, parsedItems, modeText, dishTotal, packagingFee, totalAmount);

            // 使用 UniCardBuilder 构建更新后的菜品卡片
            String cardJson = UniCardBuilder.create("dish")
                .title("订单菜品确认", "🍽️")
                .subtitle(modeText + " | 共" + totalItems + "件")
                .addDishList(dishItems)
                .addDivider()
                .addStatsRow(buildPriceStats(dishTotal, packagingFee, totalAmount))
                .addNote("您可以直接让AI帮您创建订单，或者继续调整菜品", "info")
                .addAction("确认下单", "primary", "create_order",
                    Map.of("merchantId", merchantId, "diningMode", diningMode, "totalAmount", totalAmount))
                .addAction("调整菜品", "default", "adjust_dishes",
                    Map.of("merchantId", merchantId))
                .buildJson();

            log.info("[Tool] 更新用户选择的菜品成功，总金额: {}元", totalAmount);
            return humanText + "\n\n" + cardJson;

        } catch (Exception e) {
            log.error("[Tool] 更新用户选择的菜品失败", e);
            return buildErrorText("更新失败：" + e.getMessage());
        }
    }

    // ========== 内部辅助数据结构 ==========

    /**
     * 预选菜品结果
     */
    private record PreSelectResult(Dish dish, int quantity, String reason) {}

    /**
     * 解析后的菜品项
     */
    private record ParsedDishItem(String dishId, int quantity) {}

    // ========== 商家推荐卡片构建 ==========

    /**
     * 使用 UniCardBuilder 构建商家推荐下单卡片
     */
    private String buildMerchantOrderCard(Merchant merchant, List<PreSelectResult> preSelected,
                                          String diningMode, BigDecimal estimatedTotal, String preference) {

        // 构建菜品列表
        List<UniCardDTO.DishItem> dishItems = preSelected.stream()
            .map(ps -> UniCardBuilder.createDishItem(
                ps.dish().getId(),
                ps.dish().getName(),
                ps.dish().getPrice(),
                ps.dish().getAvgRating() != null ? ps.dish().getAvgRating().doubleValue() : null,
                ps.dish().getImage(),
                ps.reason(),
                ps.dish().getCategory(),
                null
            ))
            .collect(Collectors.toList());

        // 商家信息统计
        String modeText = "dine_in".equals(diningMode) ? "堂食" : "自取";
        BigDecimal rating = merchant.getRating() != null ? merchant.getRating() : BigDecimal.ZERO;
        int estimatedTime = "takeout".equals(diningMode) ? 15 : 10;

        // 推荐理由
        String recommendationReason = buildRecommendationReason(merchant, preference, diningMode);

        return UniCardBuilder.create("merchant_order")
            .title(merchant.getName(), "🏪")
            .subtitle(String.format("%.1f分 | %s | 预计%d分钟", rating, modeText, estimatedTime))
            .addStatsRow(buildMerchantStats(merchant, estimatedTime, modeText))
            .addDivider()
            .addDishList(dishItems)
            .addDivider()
            .addStatsRow(buildPriceStats(
                calculateDishTotal(preSelected),
                calculatePackagingFee(preSelected, diningMode),
                estimatedTotal
            ))
            .addNote(recommendationReason, "info")
            .addAction("确认下单", "primary", "create_order",
                Map.of("merchantId", merchant.getId(), "diningMode", diningMode, "estimatedTotal", estimatedTotal))
            .addAction("调整菜品", "default", "adjust_dishes",
                Map.of("merchantId", merchant.getId()))
            .addAction("换一家", "default", "change_merchant",
                Map.of("diningMode", diningMode, "preference", preference != null ? preference : ""))
            .footerNote("AI为您预选了" + preSelected.size() + "道菜品，您可以自由调整")
            .buildJson();
    }

    // ========== 预选逻辑 ==========

    /**
     * 解析商家（指定ID或自动推荐）
     */
    private Merchant resolveMerchant(String merchantId) {
        if (merchantId != null && !merchantId.isEmpty()) {
            return merchantService.getById(merchantId);
        }

        // 自动推荐：取评分最高的营业中商家
        return merchantService.list().stream()
            .filter(m -> m.getStatus() != null && m.getStatus())
            .sorted((a, b) -> {
                BigDecimal ratingA = a.getRating() != null ? a.getRating() : BigDecimal.ZERO;
                BigDecimal ratingB = b.getRating() != null ? b.getRating() : BigDecimal.ZERO;
                return ratingB.compareTo(ratingA);
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * AI预选菜品，根据分类优先级和用户偏好选择
     */
    private List<PreSelectResult> preSelectDishes(List<Dish> allDishes, String preference) {
        List<PreSelectResult> selectedDishes = new ArrayList<>();

        // 按分类分组
        Map<String, List<Dish>> dishesByCategory = allDishes.stream()
            .collect(Collectors.groupingBy(d -> d.getCategory() != null ? d.getCategory() : "其他"));

        // 按分类优先级选择菜品
        String[] priorityCategories = {"主食", "热菜", "凉菜", "汤羹", "小吃"};

        for (String category : priorityCategories) {
            List<Dish> categoryDishes = dishesByCategory.get(category);
            if (categoryDishes == null || categoryDishes.isEmpty()) {
                continue;
            }

            // 按评分排序，选择前2个
            List<Dish> topDishes = categoryDishes.stream()
                .sorted((a, b) -> {
                    BigDecimal ratingA = a.getAvgRating() != null ? a.getAvgRating() : BigDecimal.ZERO;
                    BigDecimal ratingB = b.getAvgRating() != null ? b.getAvgRating() : BigDecimal.ZERO;
                    return ratingB.compareTo(ratingA);
                })
                .limit(2)
                .collect(Collectors.toList());

            for (Dish dish : topDishes) {
                String reason = buildDishRecommendationReason(dish, preference);
                selectedDishes.add(new PreSelectResult(dish, 1, reason));
            }

            // 限制最多4个菜品
            if (selectedDishes.size() >= 4) {
                break;
            }
        }

        // 如果没有选择任何菜品，至少选择评分最高的1个
        if (selectedDishes.isEmpty() && !allDishes.isEmpty()) {
            Dish topDish = allDishes.stream()
                .max(Comparator.comparing(d -> d.getAvgRating() != null ? d.getAvgRating() : BigDecimal.ZERO))
                .orElse(allDishes.get(0));
            selectedDishes.add(new PreSelectResult(topDish, 1, "这是该店最受欢迎的菜品"));
        }

        return selectedDishes;
    }

    // ========== 价格计算 ==========

    /**
     * 计算预估总价
     */
    private BigDecimal calculateEstimatedTotal(List<PreSelectResult> dishes, String diningMode) {
        BigDecimal dishTotal = calculateDishTotal(dishes);
        BigDecimal packagingFee = calculatePackagingFee(dishes, diningMode);
        return dishTotal.add(packagingFee);
    }

    /**
     * 计算菜品总价
     */
    private BigDecimal calculateDishTotal(List<PreSelectResult> dishes) {
        return dishes.stream()
            .map(ps -> ps.dish().getPrice().multiply(BigDecimal.valueOf(ps.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算包装费（仅自取收取）
     */
    private BigDecimal calculatePackagingFee(List<PreSelectResult> dishes, String diningMode) {
        boolean isTakeout = "takeout".equalsIgnoreCase(diningMode);
        if (!isTakeout) {
            return BigDecimal.ZERO;
        }
        int itemCount = dishes.stream().mapToInt(PreSelectResult::quantity).sum();
        return BigDecimal.valueOf(itemCount * 2.0);
    }

    // ========== 文本构建 ==========

    /**
     * 构建人类可读文本
     */
    private String buildHumanReadableText(Merchant merchant, List<PreSelectResult> preSelected,
                                           String diningMode, BigDecimal estimatedTotal, String preference) {
        StringBuilder text = new StringBuilder();
        BigDecimal rating = merchant.getRating() != null ? merchant.getRating() : BigDecimal.ZERO;
        String modeText = "dine_in".equals(diningMode) ? "堂食" : "自取";
        int estimatedTime = "takeout".equals(diningMode) ? 15 : 10;

        text.append("为您推荐以下商家和菜品\n\n");
        text.append(String.format("%s | %.1f分 | 预计%d分钟\n\n", merchant.getName(), rating, estimatedTime));

        text.append("AI为您预选的菜品：\n");
        for (PreSelectResult ps : preSelected) {
            BigDecimal subtotal = ps.dish().getPrice().multiply(BigDecimal.valueOf(ps.quantity()));
            text.append(String.format("  %s x %d = %.2f元 (%s)\n",
                ps.dish().getName(), ps.quantity(), subtotal, ps.reason()));
        }

        text.append(String.format("\n就餐方式：%s | 预估总价：%.2f元\n", modeText, estimatedTotal));

        if (preference != null && !preference.isEmpty()) {
            text.append(String.format("偏好标签：%s\n", preference));
        }

        return text.toString();
    }

    /**
     * 构建订单摘要文本（更新菜品时使用）
     */
    private String buildOrderSummaryText(List<UniCardDTO.DishItem> dishItems, List<ParsedDishItem> parsedItems,
                                         String modeText, BigDecimal dishTotal, BigDecimal packagingFee,
                                         BigDecimal totalAmount) {
        StringBuilder text = new StringBuilder();
        text.append("订单详情\n\n");
        text.append("菜品明细：\n");
        for (int i = 0; i < dishItems.size(); i++) {
            UniCardDTO.DishItem dish = dishItems.get(i);
            text.append(String.format("  %s x %d = %.2f元\n",
                dish.getDishName(), parsedItems.get(i).quantity, dish.getPrice()));
        }
        text.append(String.format("\n就餐方式：%s\n", modeText));
        text.append(String.format("菜品小计：%.2f元\n", dishTotal));
        text.append(String.format("包装费：%.2f元\n", packagingFee));
        text.append(String.format("应付总额：%.2f元\n", totalAmount));
        return text.toString();
    }

    // ========== 统计行构建 ==========

    /**
     * 构建商家信息统计行
     */
    private List<UniCardDTO.StatItem> buildMerchantStats(Merchant merchant, int estimatedTime, String modeText) {
        List<UniCardDTO.StatItem> items = new ArrayList<>();
        items.add(createStatItem("评分",
            merchant.getRating() != null ? merchant.getRating().toString() : "暂无", "amber", "⭐"));
        items.add(createStatItem("人均",
            merchant.getAveragePrice() != null ? merchant.getAveragePrice().toString() + "元" : "暂无", "green", "💰"));
        items.add(createStatItem("预计", estimatedTime + "分钟", "blue", "⏰"));
        items.add(createStatItem("方式", modeText, "purple", "🍽️"));
        return items;
    }

    /**
     * 构建价格统计行
     */
    private List<UniCardDTO.StatItem> buildPriceStats(BigDecimal dishTotal, BigDecimal packagingFee, BigDecimal total) {
        List<UniCardDTO.StatItem> items = new ArrayList<>();
        items.add(createStatItem("菜品小计", dishTotal.toString() + "元", "blue", "🍱"));
        items.add(createStatItem("包装费", packagingFee.toString() + "元", "orange", "📦"));
        items.add(createStatItem("应付总额", total.toString() + "元", "red", "💵"));
        return items;
    }

    /**
     * 创建统计项
     */
    private UniCardDTO.StatItem createStatItem(String label, String value, String color, String icon) {
        UniCardDTO.StatItem item = new UniCardDTO.StatItem();
        item.setLabel(label);
        item.setValue(value);
        item.setColor(color);
        item.setIcon(icon);
        return item;
    }

    // ========== 推荐理由 ==========

    /**
     * 构建菜品推荐理由
     */
    private String buildDishRecommendationReason(Dish dish, String preference) {
        List<String> reasons = new ArrayList<>();

        if (dish.getAvgRating() != null && dish.getAvgRating().compareTo(new BigDecimal("4.5")) >= 0) {
            reasons.add("评分高");
        }
        if (preference != null && !preference.isEmpty()) {
            reasons.add("符合您的口味偏好");
        }
        if (dish.getCalorie() != null && dish.getCalorie() <= 500) {
            reasons.add("热量适中");
        }

        return reasons.isEmpty() ? "推荐尝试" : String.join("，", reasons);
    }

    /**
     * 构建推荐理由
     */
    private String buildRecommendationReason(Merchant merchant, String preference, String diningMode) {
        StringBuilder reason = new StringBuilder();
        reason.append("为您推荐").append(merchant.getName());

        if (merchant.getRating() != null && merchant.getRating().compareTo(new BigDecimal("4.5")) >= 0) {
            reason.append("，评分高达").append(merchant.getRating()).append("分");
        }

        if (preference != null && !preference.isEmpty()) {
            reason.append("，根据您对「").append(preference).append("」的偏好预选了菜品");
        } else {
            reason.append("，根据热门菜品为您预选");
        }

        String modeText = "takeout".equals(diningMode) ? "自取" : "堂食";
        reason.append("，适合").append(modeText);

        return reason.toString();
    }

    // ========== 辅助方法 ==========

    /**
     * 解析用户选择的菜品JSON
     */
    private List<ParsedDishItem> parseSelectedDishes(String selectedDishesJson) {
        // 简化解析：使用 Jackson 手动解析
        List<ParsedDishItem> items = new ArrayList<>();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> rawItems = mapper.readValue(
                selectedDishesJson,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );
            for (Map<String, Object> item : rawItems) {
                String dishId = (String) item.get("dishId");
                int quantity = ((Number) item.get("quantity")).intValue();
                if (dishId != null && quantity > 0) {
                    items.add(new ParsedDishItem(dishId, quantity));
                }
            }
        } catch (Exception e) {
            log.warn("[Tool] 解析菜品列表失败: {}", e.getMessage());
        }
        return items;
    }

    /**
     * 构建错误文本响应
     */
    private String buildErrorText(String errorMessage) {
        return "错误：" + errorMessage;
    }
}
