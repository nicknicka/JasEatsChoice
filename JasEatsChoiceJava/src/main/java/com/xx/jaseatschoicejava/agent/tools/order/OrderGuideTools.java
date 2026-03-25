package com.xx.jaseatschoicejava.agent.tools.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.annotation.CardType;
import com.xx.jaseatschoicejava.agent.dto.MerchantOrderCardDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
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

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 推荐商家并生成下单卡片
     *
     * 根据用户需求推荐商家，AI自动预选菜品，生成结构化卡片数据供前端展示
     *
     * @param userId 用户ID
     * @param merchantId 商家ID（可选，如果不指定则自动推荐）
     * @param diningMode 就餐方式（dine_in=堂食 或 takeout=自取）
     * @param preference 用户偏好（可选，如"辣"、"清淡"等）
     * @return 商家下单卡片数据（JSON格式）
     */
    @Tool("""
        推荐商家并生成下单卡片

        **功能说明：**
        - 根据用户需求智能推荐商家
        - AI自动预选符合用户口味的菜品
        - 生成结构化卡片数据供前端展示
        - 支持堂食和自取两种模式

        **何时使用：**
        - 用户说"我想买xx家的东西"
        - 用户说"我想吃xxx"
        - 用户询问"有什么推荐的"
        - 用户准备下单

        **参数：**
        - userId - 用户ID（必需）
        - merchantId - 商家ID（可选，不指定则自动推荐）
        - diningMode - 就餐方式：dine_in（堂食）或 takeout（自取）
        - preference - 用户偏好，如"辣"、"清淡"、"营养均衡"（可选）

        **返回：** 商家下单卡片数据（JSON格式），包含：
        - 商家信息（ID、名称、评分、地址等）
        - AI预选的菜品列表（菜品ID、名称、价格、数量、推荐理由）
        - 就餐方式
        - 预估总价
        - 操作按钮配置

        **前端使用说明：**
        1. 解析返回的JSON数据
        2. 显示商家卡片信息
        3. 点击卡片弹出菜品选择弹窗
        4. 弹窗中显示AI预选的菜品
        5. 用户可以修改/增加菜品
        6. 用户确认后调用 createOrder 工具创建订单
        """)
    @CardType("merchant_order_card")
    public String recommendMerchantForOrder(
        @P("用户ID") String userId,
        @P(value = "商家ID（可选，不指定则自动推荐）", required = false) String merchantId,
        @P("就餐方式（dine_in=堂食 或 takeout=自取）") String diningMode,
        @P(value = "用户偏好，如：辣、清淡、营养均衡", required = false) String preference
    ) {
        log.info("🔍 [Tool] 推荐商家并生成下单卡片，userId: {}, merchantId: {}, diningMode: {}",
            userId, merchantId, diningMode);

        try {
            // 1. 验证就餐方式
            if (!diningMode.matches("^(dine_in|takeout)$")) {
                return buildErrorResponse("就餐方式错误，必须是：dine_in（堂食）或 takeout（自取）");
            }

            // 2. 获取商家信息
            Merchant merchant;
            if (merchantId != null && !merchantId.isEmpty()) {
                // 用户指定了商家
                merchant = merchantService.getById(merchantId);
                if (merchant == null) {
                    return buildErrorResponse("商家不存在，商家ID：" + merchantId);
                }
            } else {
                // 自动推荐商家（取评分最高的营业中商家）
                List<Merchant> merchants = merchantService.list().stream()
                    .filter(m -> m.getStatus() != null && m.getStatus())
                    .sorted((a, b) -> {
                        BigDecimal ratingA = a.getRating() != null ? a.getRating() : BigDecimal.ZERO;
                        BigDecimal ratingB = b.getRating() != null ? b.getRating() : BigDecimal.ZERO;
                        return ratingB.compareTo(ratingA);
                    })
                    .limit(1)
                    .collect(Collectors.toList());

                if (merchants.isEmpty()) {
                    return buildErrorResponse("暂无营业中的商家");
                }
                merchant = merchants.get(0);
                merchantId = merchant.getId();
            }

            // 3. 获取商家菜品（只取在线的）
            List<Dish> allDishes = dishService.list(
                new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getMerchantId, merchantId)
                    .eq(Dish::getIsOnline, true)
            );

            if (allDishes.isEmpty()) {
                return buildErrorResponse("该商家暂无上架菜品");
            }

            // 4. AI预选菜品（根据用户偏好）
            List<MerchantOrderCardDTO.PreSelectedDish> preSelectedDishes = preSelectDishes(
                allDishes, preference, diningMode
            );

            // 5. 计算预估总价
            BigDecimal estimatedTotal = calculateEstimatedTotal(preSelectedDishes, diningMode);

            // 6. 构建推荐理由
            String recommendationReason = buildRecommendationReason(merchant, preference, diningMode);

            // 7. 构建商家信息
            MerchantOrderCardDTO.MerchantInfo merchantInfo = new MerchantOrderCardDTO.MerchantInfo();
            merchantInfo.setMerchantId(merchant.getId());
            merchantInfo.setName(merchant.getName());
            merchantInfo.setRating(merchant.getRating() != null ? merchant.getRating().doubleValue() : null);
            merchantInfo.setAveragePrice(merchant.getAveragePrice());
            merchantInfo.setAddress(merchant.getAddress());
            merchantInfo.setDistance(300); // TODO: 实际应根据用户位置计算
            merchantInfo.setEstimatedTime(diningMode.equals("takeout") ? 15 : 10);
            merchantInfo.setIsOpen(merchant.getStatus());

            // 8. 构建操作按钮配置
            MerchantOrderCardDTO.ActionButtons actionButtons = new MerchantOrderCardDTO.ActionButtons();
            actionButtons.setPrimaryButton("确认下单");
            actionButtons.setSecondaryButton("调整菜品");
            actionButtons.setTertiaryButton("换一家");
            actionButtons.setAllowAIOrder(true);  // 允许AI帮助下单
            actionButtons.setRequirePaymentConfirmation(true);  // 付款需要用户手动确认

            // 9. 构建完整的卡片数据
            MerchantOrderCardDTO cardData = new MerchantOrderCardDTO();
            cardData.setCardType("merchant_order_card");
            cardData.setMerchant(merchantInfo);
            cardData.setPreSelectedDishes(preSelectedDishes);
            cardData.setDiningMode(diningMode);
            cardData.setEstimatedTotal(estimatedTotal);
            cardData.setActionButtons(actionButtons);
            cardData.setRecommendationReason(recommendationReason);

            // 10. 转换为JSON返回
            String cardJson = objectMapper.writeValueAsString(cardData);

            // 11. 同时返回人类可读的文本
            String humanReadableText = buildHumanReadableText(cardData);

            log.info("✅ [Tool] 推荐商家并生成下单卡片成功，merchantId: {}, 预选菜品数: {}",
                merchantId, preSelectedDishes.size());

            // 返回格式：人类可读文本 + JSON数据
            return humanReadableText + "\n\n[CARD_DATA_START]\n" + cardJson + "\n[CARD_DATA_END]";

        } catch (Exception e) {
            log.error("❌ [Tool] 推荐商家并生成下单卡片失败", e);
            return buildErrorResponse("推荐失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户选择的菜品
     *
     * 用户在前端弹窗中修改菜品后，调用此方法更新选择
     *
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @param selectedDishesJson 用户选择的菜品JSON
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
        - userId - 用户ID
        - merchantId - 商家ID
        - selectedDishesJson - 用户选择的菜品（JSON格式）
          格式：[{"dishId":"D001","quantity":2},{"dishId":"D002","quantity":1}]
        - diningMode - 就餐方式（dine_in=堂食 或 takeout=自取）

        **返回：** 更新后的订单信息和价格明细
        """)
    public String updateSelectedDishes(
        @P("用户ID") String userId,
        @P("商家ID") String merchantId,
        @P("用户选择的菜品（JSON格式）") String selectedDishesJson,
        @P("就餐方式（dine_in=堂食 或 takeout=自取）") String diningMode
    ) {
        log.info("🔍 [Tool] 更新用户选择的菜品，userId: {}, merchantId: {}", userId, merchantId);

        try {
            // 1. 解析菜品列表
            List<Map<String, Object>> selectedDishes;
            try {
                selectedDishes = objectMapper.readValue(
                    selectedDishesJson,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );
            } catch (JsonProcessingException e) {
                return buildErrorResponse("菜品列表格式错误：" + e.getMessage());
            }

            if (selectedDishes.isEmpty()) {
                return buildErrorResponse("请至少选择一道菜品");
            }

            // 2. 获取菜品详细信息
            StringBuilder orderDetails = new StringBuilder();
            BigDecimal dishTotal = BigDecimal.ZERO;
            int totalItems = 0;

            orderDetails.append("📋 **订单详情**\n\n");

            for (Map<String, Object> item : selectedDishes) {
                String dishId = (String) item.get("dishId");
                Integer quantity = ((Number) item.get("quantity")).intValue();

                Dish dish = dishService.getById(dishId);
                if (dish == null) {
                    continue;
                }

                BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(quantity));
                dishTotal = dishTotal.add(subtotal);
                totalItems += quantity;

                orderDetails.append(String.format(
                    "• %s × %d份 = %.2f元\n",
                    dish.getName(), quantity, subtotal
                ));
            }

            // 3. 计算其他费用
            boolean isTakeout = "takeout".equalsIgnoreCase(diningMode);
            BigDecimal packagingFee = isTakeout
                ? BigDecimal.valueOf(totalItems * 2.0)
                : BigDecimal.ZERO;
            BigDecimal totalAmount = dishTotal.add(packagingFee);

            // 4. 构建返回信息
            String result = String.format(
                "%s" +
                "💰 **金额明细**\n" +
                "• 菜品小计：%.2f元\n" +
                "• 包装费：%.2f元\n" +
                "• 应付总额：%.2f元\n\n" +
                "💡 您可以直接让AI帮您创建订单，或者继续调整菜品",
                orderDetails.toString(),
                dishTotal,
                packagingFee,
                totalAmount
            );

            log.info("✅ [Tool] 更新用户选择的菜品成功，总金额: {}元", totalAmount);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 更新用户选择的菜品失败", e);
            return buildErrorResponse("更新失败：" + e.getMessage());
        }
    }

    /**
     * AI预选菜品
     *
     * 根据用户偏好和AI推荐算法，自动选择合适的菜品
     *
     * @param allDishes 商家所有菜品
     * @param preference 用户偏好
     * @param diningMode 就餐方式
     * @return 预选菜品列表
     */
    private List<MerchantOrderCardDTO.PreSelectedDish> preSelectDishes(
        List<Dish> allDishes,
        String preference,
        String diningMode
    ) {
        List<MerchantOrderCardDTO.PreSelectedDish> selectedDishes = new ArrayList<>();

        // 按分类分组
        Map<String, List<Dish>> dishesByCategory = allDishes.stream()
            .collect(Collectors.groupingBy(d -> d.getCategory() != null ? d.getCategory() : "其他"));

        // AI推荐逻辑：从每个主要分类中选择1-2个高分菜品
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
                // 根据用户偏好决定是否选择该菜品
                if (shouldSelectDish(dish, preference)) {
                    MerchantOrderCardDTO.PreSelectedDish preSelectedDish = new MerchantOrderCardDTO.PreSelectedDish();
                    preSelectedDish.setDishId(dish.getId());
                    preSelectedDish.setDishName(dish.getName());
                    preSelectedDish.setPrice(dish.getPrice());
                    preSelectedDish.setQuantity(1);  // 默认数量为1
                    preSelectedDish.setCalories(dish.getCalorie());
                    preSelectedDish.setImageUrl(dish.getImage());
                    preSelectedDish.setReason(buildDishRecommendationReason(dish, preference));
                    preSelectedDish.setCategory(dish.getCategory());
                    selectedDishes.add(preSelectedDish);
                }
            }

            // 限制最多选择4个菜品
            if (selectedDishes.size() >= 4) {
                break;
            }
        }

        // 如果没有选择任何菜品，至少选择评分最高的1个
        if (selectedDishes.isEmpty() && !allDishes.isEmpty()) {
            Dish topDish = allDishes.stream()
                .max(Comparator.comparing(d -> d.getAvgRating() != null ? d.getAvgRating() : BigDecimal.ZERO))
                .orElse(allDishes.get(0));

            MerchantOrderCardDTO.PreSelectedDish preSelectedDish = new MerchantOrderCardDTO.PreSelectedDish();
            preSelectedDish.setDishId(topDish.getId());
            preSelectedDish.setDishName(topDish.getName());
            preSelectedDish.setPrice(topDish.getPrice());
            preSelectedDish.setQuantity(1);
            preSelectedDish.setCalories(topDish.getCalorie());
            preSelectedDish.setImageUrl(topDish.getImage());
            preSelectedDish.setReason("这是该店最受欢迎的菜品");
            preSelectedDish.setCategory(topDish.getCategory());
            selectedDishes.add(preSelectedDish);
        }

        return selectedDishes;
    }

    /**
     * 判断是否应该选择该菜品
     */
    private boolean shouldSelectDish(Dish dish, String preference) {
        // 如果用户没有特殊偏好，默认选择
        if (preference == null || preference.isEmpty()) {
            return true;
        }

        // TODO: 可以根据菜品描述、标签等更智能地判断
        // 这里简化处理，默认返回true
        return true;
    }

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
     * 计算预估总价
     */
    private BigDecimal calculateEstimatedTotal(
        List<MerchantOrderCardDTO.PreSelectedDish> dishes,
        String diningMode
    ) {
        BigDecimal dishTotal = dishes.stream()
            .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean isTakeout = "takeout".equalsIgnoreCase(diningMode);
        int itemCount = dishes.stream().mapToInt(d -> d.getQuantity()).sum();
        BigDecimal packagingFee = isTakeout
            ? BigDecimal.valueOf(itemCount * 2.0)
            : BigDecimal.ZERO;

        return dishTotal.add(packagingFee);
    }

    /**
     * 构建推荐理由
     */
    private String buildRecommendationReason(Merchant merchant, String preference, String diningMode) {
        StringBuilder reason = new StringBuilder();

        reason.append("我为您推荐了").append(merchant.getName());

        if (merchant.getRating() != null && merchant.getRating().compareTo(new BigDecimal("4.5")) >= 0) {
            reason.append("，该店评分高达").append(merchant.getRating()).append("分");
        }

        if (preference != null && !preference.isEmpty()) {
            reason.append("，并根据您对").append(preference).append("的偏好预选了菜品");
        } else {
            reason.append("，并根据该店热门菜品为您预选了");
        }

        String modeText = diningMode.equals("takeout") ? "自取" : "堂食";
        reason.append("，适合").append(modeText);

        return reason.toString();
    }

    /**
     * 构建人类可读的文本
     */
    private String buildHumanReadableText(MerchantOrderCardDTO cardData) {
        StringBuilder text = new StringBuilder();

        text.append("🛒 **为您推荐以下商家和菜品**\n\n");

        // 商家信息
        MerchantOrderCardDTO.MerchantInfo merchant = cardData.getMerchant();
        text.append(String.format(
            "🏪 **%s**\n",
            merchant.getName()
        ));
        text.append(String.format(
            "   ⭐ %.1f分 | 📍 距离约%d米 | ⏰ 预计%d分钟\n\n",
            merchant.getRating() != null ? merchant.getRating() : 0,
            merchant.getDistance(),
            merchant.getEstimatedTime()
        ));

        // 预选菜品
        text.append("🍽️ **AI为您预选的菜品：**\n\n");
        for (MerchantOrderCardDTO.PreSelectedDish dish : cardData.getPreSelectedDishes()) {
            text.append(String.format(
                "• **%s** × %d份 = %.2f元\n",
                dish.getDishName(),
                dish.getQuantity(),
                dish.getPrice().multiply(BigDecimal.valueOf(dish.getQuantity()))
            ));
            text.append(String.format(
                "  %s\n\n",
                dish.getReason()
            ));
        }

        // 价格信息
        String modeText = "dine_in".equals(cardData.getDiningMode()) ? "堂食" : "自取";
        text.append(String.format(
            "🍽️ 就餐方式：%s\n",
            modeText
        ));
        text.append(String.format(
            "💰 预估总价：**%.2f元**\n\n",
            cardData.getEstimatedTotal()
        ));

        // 推荐理由
        text.append(String.format(
            "💡 %s\n\n",
            cardData.getRecommendationReason()
        ));

        text.append("👇 **您可以：**\n");
        text.append("1. 直接确认下单\n");
        text.append("2. 点击卡片调整菜品\n");
        text.append("3. 让AI帮您换一家");

        return text.toString();
    }

    /**
     * 构建错误响应
     */
    private String buildErrorResponse(String errorMessage) {
        return "❌ " + errorMessage;
    }
}
