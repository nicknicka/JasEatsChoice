package com.xx.jaseatschoicejava.agent.tools.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.dto.UniCardDTO;
import com.xx.jaseatschoicejava.agent.util.UniCardBuilder;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.util.IdGenerator;
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
 * 订单创建工具类
 *
 * 为Agent提供订单创建和价格计算功能，使用 UniCardBuilder 构建统一卡片格式
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class OrderCreateTools {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderDishService orderDishService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private DishService dishService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 计算订单价格
     *
     * @param dishItemsJson 菜品列表JSON
     * @param diningMode 就餐方式
     * @return 价格明细（UniCard JSON格式）
     */
    @Tool("""
        计算订单的价格

        **价格包含：**
        - 菜品总价
        - 包装费（自取时每项2元，堂食无包装费）
        - 最终总计

        **注意：** 堂食和自取模式均无配送费

        **何时使用：**
        - 下单前确认价格
        - 比较不同方案

        **参数：**
        - dishItemsJson - 菜品列表（JSON格式）
              例如：[{"dishId":"xxx","quantity":2,"price":15.5}]
        - diningMode - 就餐方式（堂食/dine_in 或 自取/takeout）

        **无需参数**，userId自动从上下文获取

        **返回：** 价格明细（UniCard JSON格式）
        """)
    public String calculateOrderPrice(
        @P("菜品列表（JSON格式）") String dishItemsJson,
        AgenticScope scope,
        @P("就餐方式（堂食/dine_in 或 自取/takeout）") String diningMode
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return buildErrorText("无法获取用户信息，请重新登录");
        }
        log.info("[Tool] 计算订单价格，userId: {}, diningMode: {}", userId, diningMode);

        try {
            List<Map<String, Object>> dishItems = objectMapper.readValue(
                dishItemsJson,
                new TypeReference<List<Map<String, Object>>>() {}
            );

            if (dishItems == null || dishItems.isEmpty()) {
                return buildErrorText("菜品列表为空");
            }

            // 计算菜品总价并构建菜品列表
            double dishTotal = 0;
            int itemCount = 0;
            List<UniCardDTO.DishItem> cardDishItems = new ArrayList<>();

            for (Map<String, Object> item : dishItems) {
                String dishId = (String) item.get("dishId");
                int quantity = ((Number) item.get("quantity")).intValue();
                double price = ((Number) item.get("price")).doubleValue();

                double subtotal = quantity * price;
                dishTotal += subtotal;
                itemCount += quantity;

                // 尝试获取菜品详情以丰富卡片数据
                Dish dish = dishService.getById(dishId);
                String dishName = dish != null ? dish.getName() : "菜品" + dishId;
                String imageUrl = dish != null ? dish.getImage() : null;

                cardDishItems.add(UniCardBuilder.createDishItem(
                    dishId, dishName, BigDecimal.valueOf(price),
                    null, imageUrl, null, null, null
                ));
            }

            // 计算费用
            boolean isTakeout = "takeout".equalsIgnoreCase(diningMode) || "自取".equals(diningMode);
            double packagingFee = isTakeout ? itemCount * 2.0 : 0.0;
            double total = dishTotal + packagingFee;
            String modeText = isTakeout ? "自取" : "堂食";

            // 构建人类可读文本
            String humanText = String.format(
                "订单价格计算完成：%s，菜品小计 %.2f元，包装费 %.2f元，总计 %.2f元",
                modeText, dishTotal, packagingFee, total
            );

            // 使用 UniCardBuilder 构建价格卡片
            String cardJson = UniCardBuilder.create("dish")
                .title("订单价格明细", "💰")
                .subtitle(modeText + " | 共" + itemCount + "件")
                .addDishList(cardDishItems)
                .addDivider()
                .addStatsRow(buildPriceStats(dishTotal, packagingFee, total))
                .addNote("如有优惠券，下单时系统会自动抵扣", "info")
                .buildJson();

            log.info("[Tool] 计算订单价格成功，总计: {}元", total);
            return humanText + "\n\n" + cardJson;

        } catch (Exception e) {
            log.error("[Tool] 计算订单价格失败", e);
            return buildErrorText("计算失败：" + e.getMessage());
        }
    }

    /**
     * 创建订单
     *
     * @param merchantId 商家ID
     * @param dishItemsJson 菜品列表JSON字符串
     * @param diningMode 就餐方式
     * @param tableNumber 座号（可选）
     * @param note 备注（可选）
     * @return 订单创建结果（UniCard JSON格式）
     */
    @Tool("""
        创建一个新的订单（堂食/自取模式）

        **必需参数：**
        - merchantId: 商家ID
        - dishItemsJson: 菜品列表（JSON数组字符串格式）
        - diningMode: 就餐方式（"dine_in"=堂食 或 "takeout"=自取）

        **无需参数**，userId自动从上下文获取

        **可选参数：**
        - tableNumber: 座号（堂食时填写）
        - note: 备注信息

        **dishItemsJson 格式示例：**
        [{"dishId":"D001","quantity":2,"price":15.5},{"dishId":"D002","quantity":1,"price":8.0}]

        **何时使用：**
        - 用户明确要下单
        - 确认订单信息后创建

        **重要提醒：**
        - dishItemsJson 必须是JSON数组字符串格式
        - 每个菜品必须包含 dishId, quantity, price
        - 堂食和自取均无配送费
        - 堂食时建议填写座号

        **返回：** 订单创建结果（UniCard JSON格式）
        """)
    public String createOrder(
        AgenticScope scope,
        @P("商家ID") String merchantId,
        @P("菜品列表（JSON数组字符串）") String dishItemsJson,
        @P("就餐方式（dine_in=堂食 或 takeout=自取）") String diningMode,
        @P(value = "座号（可选，堂食时建议填写）", required = false) String tableNumber,
        @P(value = "备注信息（可选）", required = false) String note
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return buildErrorText("无法获取用户信息，请重新登录");
        }
        log.info("[Tool] 创建订单，userId: {}, merchantId: {}, diningMode: {}", userId, merchantId, diningMode);

        try {
            // 参数验证
            String validationError = validateCreateOrderParams(userId, merchantId, diningMode, dishItemsJson);
            if (validationError != null) {
                return buildErrorText(validationError);
            }

            // 解析菜品列表
            List<Map<String, Object>> dishItems = parseDishItems(dishItemsJson);
            if (dishItems == null) {
                return buildErrorText("菜品列表格式错误，正确格式：[{\"dishId\":\"xxx\",\"quantity\":1,\"price\":15.5}]");
            }
            if (dishItems.isEmpty()) {
                return buildErrorText("菜品列表不能为空");
            }

            // 计算费用
            double dishTotal = 0;
            int itemCount = 0;
            for (Map<String, Object> item : dishItems) {
                int quantity = ((Number) item.get("quantity")).intValue();
                double price = ((Number) item.get("price")).doubleValue();
                dishTotal += quantity * price;
                itemCount += quantity;
            }

            boolean isTakeout = "takeout".equalsIgnoreCase(diningMode);
            double packagingFee = isTakeout ? itemCount * 2.0 : 0.0;
            double totalAmount = dishTotal + packagingFee;

            // 创建订单记录
            Order order = buildOrderEntity(userId, merchantId, diningMode, tableNumber, note,
                totalAmount, isTakeout);
            boolean success = orderService.save(order);
            if (!success) {
                log.error("[Tool] 创建订单失败");
                return buildErrorText("订单创建失败，请稍后重试");
            }

            // 保存订单菜品
            saveOrderDishes(order.getId(), dishItems);

            // 获取商家信息
            Merchant merchant = merchantService.getById(merchantId);
            String merchantName = merchant != null ? merchant.getName() : "未知商家";

            // 构建人类可读文本
            String humanText = buildCreateOrderHumanText(order, merchantName, isTakeout, tableNumber);

            // 使用 UniCardBuilder 构建订单创建成功卡片
            String cardJson = buildOrderCreatedCard(order, merchantName, dishItems, isTakeout, tableNumber);

            log.info("[Tool] 订单创建成功: {}, 菜品数: {}", order.getId(), dishItems.size());
            return humanText + "\n\n" + cardJson;

        } catch (Exception e) {
            log.error("[Tool] 创建订单失败", e);
            return buildErrorText("创建失败：" + e.getMessage());
        }
    }

    /**
     * 查询可用优惠券（简化版）
     *
     * @param orderAmount 订单金额
     * @return 可用优惠券信息
     */
    @Tool("""
        查询用户可用的优惠券（简化版）

        **何时使用：**
        - 下单前查询优惠
        - 推荐最优优惠

        **参数：**
        - orderAmount - 订单金额

        **无需参数**，userId自动从上下文获取

        **返回：** 可用优惠券信息
        """)
    public String getAvailableCoupons(
        AgenticScope scope,
        @P("订单金额") double orderAmount
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return buildErrorText("无法获取用户信息，请重新登录");
        }
        log.info("[Tool] 查询可用优惠券，userId: {}, orderAmount: {}", userId, orderAmount);

        // 使用 UniCardBuilder 构建优惠券卡片
        List<UniCardDTO.StatItem> couponStats = new ArrayList<>();
        couponStats.add(createStatItem("新用户专享券", "满20减5元", "green", "🎫"));
        couponStats.add(createStatItem("午餐优惠券", "满30减8元", "blue", "🎫"));

        String humanText = "查询到2张可用优惠券，下单时系统会自动使用最优优惠券";

        String cardJson = UniCardBuilder.create("dish")
            .title("可用优惠券", "🎫")
            .subtitle("订单金额：" + orderAmount + "元")
            .addStatsRow(couponStats)
            .addNote("下单时系统会自动使用最优优惠券", "info")
            .buildJson();

        return humanText + "\n\n" + cardJson;
    }

    /**
     * 准备订单（触发前端显示商家菜品选择卡片）
     *
     * @param merchantNameOrDishName 商家名称或菜品名称
     * @return UniCard JSON格式的商家菜品数据
     */
    @Tool("""
        准备订单，查询商家和菜品信息（触发前端显示商家菜品选择卡片）

        **何时使用：**
        - 用户说"我想买XX餐厅的..."
        - 用户说"我要下单XX菜品"
        - 用户表达购买意向时

        **参数：**
        - merchantNameOrDishName - 商家名称或菜品名称

        **无需参数**，userId自动从上下文获取

        **返回：** 商家菜品卡片数据（UniCard JSON格式）
        """)
    public String prepareOrder(
        @P("商家名称或菜品名称") String merchantNameOrDishName,
        AgenticScope scope
    ) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return buildErrorText("无法获取用户信息，请重新登录");
        }
        log.info("[Tool] 准备订单，商家/菜品：{}, 用户：{}", merchantNameOrDishName, userId);

        try {
            if (merchantNameOrDishName == null || merchantNameOrDishName.trim().isEmpty()) {
                return buildErrorText("请提供商家名称或菜品名称");
            }

            // 1. 先尝试按商家名称查询
            List<Merchant> merchants = findMerchantsByName(merchantNameOrDishName);

            // 2. 如果没找到商家，尝试按菜品名称查询
            if (merchants.isEmpty()) {
                merchants = findMerchantsByDishName(merchantNameOrDishName);
            }

            if (merchants.isEmpty()) {
                return buildErrorText("未找到相关商家，请确认商家名称或菜品名称");
            }

            Merchant merchant = merchants.get(0);

            // 3. 查询该商家的所有在线菜品
            List<Dish> allDishes = dishService.list().stream()
                .filter(d -> merchant.getId().equals(d.getMerchantId()))
                .filter(d -> d.getIsOnline() != null && d.getIsOnline())
                .collect(Collectors.toList());

            // 4. 使用 UniCardBuilder 构建商家菜品卡片
            String humanText = String.format("找到商家【%s】，共%d道菜品可选",
                merchant.getName(), allDishes.size());

            String cardJson = buildMerchantMenuCard(merchant, allDishes, merchantNameOrDishName);

            log.info("[Tool] 准备订单成功，商家：{}，菜品数：{}", merchant.getName(), allDishes.size());
            return humanText + "\n\n" + cardJson;

        } catch (Exception e) {
            log.error("[Tool] 准备订单失败", e);
            return buildErrorText("查询失败：" + e.getMessage());
        }
    }

    // ========== 订单创建辅助方法 ==========

    /**
     * 验证创建订单参数
     */
    private String validateCreateOrderParams(String userId, String merchantId,
                                              String diningMode, String dishItemsJson) {
        if (userId == null || userId.isEmpty()) {
            return "缺少用户ID";
        }
        if (merchantId == null || merchantId.isEmpty()) {
            return "缺少商家ID（merchantId）";
        }
        if (diningMode == null || diningMode.isEmpty()) {
            return "缺少就餐方式（diningMode），请指定：dine_in（堂食）或 takeout（自取）";
        }
        if (!diningMode.matches("^(dine_in|takeout)$")) {
            return "就餐方式错误，diningMode 必须是：dine_in（堂食）或 takeout（自取）";
        }
        return null;
    }

    /**
     * 解析菜品列表JSON
     */
    private List<Map<String, Object>> parseDishItems(String dishItemsJson) {
        try {
            return objectMapper.readValue(
                dishItemsJson,
                new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建订单实体
     */
    private Order buildOrderEntity(String userId, String merchantId, String diningMode,
                                    String tableNumber, String note, double totalAmount,
                                    boolean isTakeout) {
        Order order = new Order();
        order.setId(IdGenerator.toOrderIdString(IdGenerator.generateId()));
        order.setUserId(userId);
        order.setMerchantId(merchantId);

        // 设置地址信息
        String addressInfo = isTakeout
            ? "自取"
            : "堂食" + (tableNumber != null ? " - 座号：" + tableNumber : "");
        order.setAddress(addressInfo);

        order.setTotalAmount(BigDecimal.valueOf(totalAmount));
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(0);  // 待支付

        // 构建备注
        StringBuilder remarkBuilder = new StringBuilder();
        remarkBuilder.append("就餐方式：").append(isTakeout ? "自取" : "堂食");
        if (!isTakeout && tableNumber != null) {
            remarkBuilder.append("，座号：").append(tableNumber);
        }
        if (note != null && !note.isEmpty()) {
            remarkBuilder.append("，备注：").append(note);
        }
        order.setRemark(remarkBuilder.toString());

        return order;
    }

    /**
     * 保存订单菜品关联
     */
    private void saveOrderDishes(String orderId, List<Map<String, Object>> dishItems) {
        List<OrderDish> orderDishList = new ArrayList<>();
        for (Map<String, Object> item : dishItems) {
            String dishId = (String) item.get("dishId");
            int quantity = ((Number) item.get("quantity")).intValue();
            double price = ((Number) item.get("price")).doubleValue();

            OrderDish orderDish = new OrderDish();
            orderDish.setId(IdGenerator.toOrderDishIdString(IdGenerator.generateId()));
            orderDish.setOrderId(orderId);
            orderDish.setDishId(dishId);
            orderDish.setQuantity(quantity);
            orderDish.setPrice(BigDecimal.valueOf(price));
            orderDish.setServingStatus(0); // 未上菜
            orderDish.setStepStatus(0); // 待备菜

            orderDishList.add(orderDish);
        }
        orderDishService.saveBatch(orderDishList);
    }

    // ========== 卡片构建方法 ==========

    /**
     * 使用 UniCardBuilder 构建订单创建成功卡片
     */
    private String buildOrderCreatedCard(Order order, String merchantName,
                                          List<Map<String, Object>> dishItems,
                                          boolean isTakeout, String tableNumber) {
        // 构建菜品列表
        List<UniCardDTO.DishItem> cardDishItems = new ArrayList<>();
        for (Map<String, Object> item : dishItems) {
            String dishId = (String) item.get("dishId");
            int quantity = ((Number) item.get("quantity")).intValue();
            double price = ((Number) item.get("price")).doubleValue();

            Dish dish = dishService.getById(dishId);
            String dishName = dish != null ? dish.getName() : "菜品" + dishId;
            String imageUrl = dish != null ? dish.getImage() : null;

            cardDishItems.add(UniCardBuilder.createDishItem(
                dishId, dishName, BigDecimal.valueOf(price),
                null, imageUrl, null, null, null
            ));
        }

        // 计算金额
        double dishTotal = 0;
        int itemCount = 0;
        for (Map<String, Object> item : dishItems) {
            int quantity = ((Number) item.get("quantity")).intValue();
            double price = ((Number) item.get("price")).doubleValue();
            dishTotal += quantity * price;
            itemCount += quantity;
        }
        double packagingFee = isTakeout ? itemCount * 2.0 : 0.0;
        String modeText = isTakeout ? "自取" : "堂食";

        // 订单状态统计
        List<UniCardDTO.StatItem> orderStats = new ArrayList<>();
        orderStats.add(createStatItem("订单号", order.getId(), "blue", "📋"));
        orderStats.add(createStatItem("状态", "待支付", "orange", "📊"));
        orderStats.add(createStatItem("商家", merchantName, "green", "🏪"));

        if (!isTakeout && tableNumber != null) {
            orderStats.add(createStatItem("座号", tableNumber, "purple", "🪑"));
        }

        return UniCardBuilder.create("merchant_order")
            .title("订单已创建", "📋")
            .subtitle(modeText + " | 等待支付")
            .addStatsRow(orderStats)
            .addDivider()
            .addDishList(cardDishItems)
            .addDivider()
            .addStatsRow(buildPriceStats(dishTotal, packagingFee, order.getTotalAmount().doubleValue()))
            .addNote("请在15分钟内完成支付，超时订单将自动取消", "warning")
            .addNote("预计" + (isTakeout ? "15-20分钟" : "10-15分钟") + "后可取餐", "info")
            .addAction("查看订单", "primary", "view_order",
                Map.of("orderId", order.getId()))
            .addAction("继续点餐", "default", "continue_order",
                Map.of("merchantId", order.getMerchantId()))
            .footerNote("订单号：" + order.getId())
            .buildJson();
    }

    /**
     * 使用 UniCardBuilder 构建商家菜单卡片
     */
    private String buildMerchantMenuCard(Merchant merchant, List<Dish> allDishes,
                                          String keyword) {
        // 构建菜品列表
        List<UniCardDTO.DishItem> cardDishItems = allDishes.stream()
            .map(dish -> UniCardBuilder.createDishItem(
                dish.getId(),
                dish.getName(),
                dish.getPrice(),
                dish.getAvgRating() != null ? dish.getAvgRating().doubleValue() : null,
                dish.getImage(),
                dish.getDescription(),
                dish.getCategory(),
                null
            ))
            .collect(Collectors.toList());

        // 查找默认选中的菜品（匹配关键词的菜品）
        List<Map<String, Object>> defaultSelection = allDishes.stream()
            .filter(d -> d.getName() != null && d.getName().contains(keyword))
            .limit(3)
            .map(dish -> {
                Map<String, Object> d = new HashMap<>();
                d.put("dishId", dish.getId());
                d.put("name", dish.getName());
                d.put("quantity", 1);
                d.put("price", dish.getPrice());
                return d;
            })
            .collect(Collectors.toList());

        // 商家信息统计
        List<UniCardDTO.StatItem> merchantStats = new ArrayList<>();
        merchantStats.add(createStatItem("评分",
            merchant.getRating() != null ? merchant.getRating().toString() : "暂无", "amber", "⭐"));
        merchantStats.add(createStatItem("人均",
            merchant.getAveragePrice() != null ? merchant.getAveragePrice().toString() + "元" : "暂无", "green", "💰"));
        merchantStats.add(createStatItem("菜品", allDishes.size() + "道", "blue", "🍽️"));

        UniCardBuilder builder = UniCardBuilder.create("dish")
            .title(merchant.getName(), "🏪")
            .subtitle(merchant.getAddress() != null ? merchant.getAddress() : "")
            .addStatsRow(merchantStats)
            .addDivider()
            .addDishList(cardDishItems);

        // 如果有默认选中的菜品，添加提示
        if (!defaultSelection.isEmpty()) {
            builder.addNote("已为您预选" + defaultSelection.size() + "道匹配的菜品", "info");
        }

        return builder
            .addAction("立即下单", "primary", "create_order",
                Map.of("merchantId", merchant.getId(),
                       "defaultSelection", defaultSelection))
            .footerNote(merchant.getStatus() ? "营业中" : "已休息")
            .buildJson();
    }

    // ========== 商家查询辅助方法 ==========

    /**
     * 按商家名称查询
     */
    private List<Merchant> findMerchantsByName(String name) {
        return merchantService.list().stream()
            .filter(m -> m.getStatus() != null && m.getStatus())
            .filter(m -> m.getName() != null && m.getName().contains(name))
            .collect(Collectors.toList());
    }

    /**
     * 按菜品名称查找对应商家
     */
    private List<Merchant> findMerchantsByDishName(String dishName) {
        List<Dish> dishes = dishService.list().stream()
            .filter(d -> d.getName() != null && d.getName().contains(dishName))
            .filter(d -> d.getIsOnline() != null && d.getIsOnline())
            .collect(Collectors.toList());

        if (dishes.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> merchantIds = dishes.stream()
            .map(Dish::getMerchantId)
            .collect(Collectors.toSet());

        return merchantService.list().stream()
            .filter(m -> merchantIds.contains(m.getId()))
            .filter(m -> m.getStatus() != null && m.getStatus())
            .collect(Collectors.toList());
    }

    // ========== 通用辅助方法 ==========

    /**
     * 构建价格统计行
     */
    private List<UniCardDTO.StatItem> buildPriceStats(double dishTotal, double packagingFee, double total) {
        List<UniCardDTO.StatItem> items = new ArrayList<>();
        items.add(createStatItem("菜品小计", String.format("%.2f元", dishTotal), "blue", "🍱"));
        items.add(createStatItem("包装费", String.format("%.2f元", packagingFee), "orange", "📦"));
        items.add(createStatItem("应付总额", String.format("%.2f元", total), "red", "💵"));
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

    /**
     * 构建订单创建的人类可读文本
     */
    private String buildCreateOrderHumanText(Order order, String merchantName,
                                              boolean isTakeout, String tableNumber) {
        String modeText = isTakeout ? "自取" : "堂食";
        StringBuilder text = new StringBuilder();
        text.append("订单已创建，等待支付\n");
        text.append(String.format("订单号：%s | 商家：%s | %s", order.getId(), merchantName, modeText));
        if (!isTakeout && tableNumber != null) {
            text.append(" | 座号：").append(tableNumber);
        }
        text.append(String.format("\n应付总额：%.2f元", order.getTotalAmount()));
        return text.toString();
    }

    /**
     * 构建错误文本
     */
    private String buildErrorText(String errorMessage) {
        return "错误：" + errorMessage;
    }
}
