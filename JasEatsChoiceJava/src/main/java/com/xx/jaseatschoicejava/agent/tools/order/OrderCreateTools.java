package com.xx.jaseatschoicejava.agent.tools.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.util.IdGenerator;
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
 * 为Agent提供订单创建和价格计算功能
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
     * @param userId 用户ID
     * @return 价格明细
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
        - userId - 用户ID
        - diningMode - 就餐方式（堂食/dine_in 或 自取/takeout）

        **返回：** 价格明细（文本格式）
        """)
    public String calculateOrderPrice(
        @P("菜品列表（JSON格式）") String dishItemsJson,
        @P("用户ID") String userId,
        @P("就餐方式（堂食/dine_in 或 自取/takeout）") String diningMode
    ) {
        log.info("🔍 [Tool] 计算订单价格，userId: {}, diningMode: {}", userId, diningMode);

        try {
            List<Map<String, Object>> dishItems = objectMapper.readValue(
                dishItemsJson,
                new TypeReference<List<Map<String, Object>>>() {}
            );

            if (dishItems == null || dishItems.isEmpty()) {
                return "❌ 菜品列表为空";
            }

            // 计算菜品总价
            double dishTotal = 0;
            int itemCount = 0;

            StringBuilder itemsDetail = new StringBuilder();
            itemsDetail.append("📋 菜品明细：\n");

            for (Map<String, Object> item : dishItems) {
                String dishId = (String) item.get("dishId");
                Integer quantity = ((Number) item.get("quantity")).intValue();
                Double price = ((Number) item.get("price")).doubleValue();

                double subtotal = quantity * price;
                dishTotal += subtotal;
                itemCount += quantity;

                itemsDetail.append(String.format(
                    "  - 菜品ID: %s × %d = %.2f元\n",
                    dishId, quantity, subtotal
                ));
            }

            // 计算其他费用
            boolean isTakeout = "takeout".equalsIgnoreCase(diningMode) || "自取".equals(diningMode);
            double deliveryFee = 0.0;  // 堂食和自取均无配送费
            double packagingFee = isTakeout ? itemCount * 2.0 : 0.0;  // 仅自取收取包装费
            double total = dishTotal + deliveryFee + packagingFee;

            String modeText = isTakeout ? "🥡 自取" : "🍽️ 堂食";

            String result = String.format(
                "💰 订单价格明细\n\n" +
                "%s" +
                "🍽️ 就餐方式：%s\n" +
                "🍱 菜品小计：%.2f元\n" +
                "📦 包装费：%.2f元\n" +
                "\n" +
                "─".repeat(30) + "\n" +
                "💵 **总计：%.2f元**\n\n" +
                "💡 下单时如有优惠券，系统会自动抵扣",
                itemsDetail.toString(),
                modeText,
                dishTotal,
                packagingFee,
                total
            );

            log.info("✅ [Tool] 计算订单价格成功，总计: {}元", total);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 计算订单价格失败", e);
            return "❌ 计算失败：" + e.getMessage();
        }
    }

    /**
     * 创建订单
     *
     * 使用多个独立参数，便于AI正确调用
     *
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @param dishItemsJson 菜品列表JSON字符串
     * @param diningMode 就餐方式
     * @param tableNumber 座号（可选）
     * @param note 备注（可选）
     * @return 订单创建结果
     */
    @Tool("""
        创建一个新的订单（堂食/自取模式）

        **必需参数：**
        - userId: 用户ID
        - merchantId: 商家ID
        - dishItemsJson: 菜品列表（JSON数组字符串格式）
        - diningMode: 就餐方式（"dine_in"=堂食 或 "takeout"=自取）

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

        **返回：** 订单创建结果
        """)
    public String createOrder(
        @P("用户ID") String userId,
        @P("商家ID") String merchantId,
        @P("菜品列表（JSON数组字符串）") String dishItemsJson,
        @P("就餐方式（dine_in=堂食 或 takeout=自取）") String diningMode,
        @P(value = "座号（可选，堂食时建议填写）", required = false) String tableNumber,
        @P(value = "备注信息（可选）", required = false) String note
    ) {
        log.info("🔍 [Tool] 创建订单，userId: {}, merchantId: {}, diningMode: {}", userId, merchantId, diningMode);

        try {
            // 验证必需参数
            if (userId == null || userId.isEmpty()) {
                return "❌ 缺少用户ID（userId）";
            }
            if (merchantId == null || merchantId.isEmpty()) {
                return "❌ 缺少商家ID（merchantId）";
            }
            if (diningMode == null || diningMode.isEmpty()) {
                return "❌ 缺少就餐方式（diningMode），请指定：dine_in（堂食）或 takeout（自取）";
            }

            // 验证就餐方式
            if (!diningMode.matches("^(dine_in|takeout)$")) {
                return "❌ 就餐方式错误，diningMode 必须是：dine_in（堂食）或 takeout（自取）";
            }

            // 解析菜品列表
            List<Map<String, Object>> dishItems;
            try {
                dishItems = objectMapper.readValue(
                    dishItemsJson,
                    new TypeReference<List<Map<String, Object>>>() {}
                );
            } catch (Exception e) {
                return "❌ 菜品列表格式错误，正确格式：[{\"dishId\":\"xxx\",\"quantity\":1,\"price\":15.5}]";
            }

            if (dishItems == null || dishItems.isEmpty()) {
                return "❌ 菜品列表不能为空";
            }

            double dishTotal = 0;
            int itemCount = 0;
            for (Map<String, Object> item : dishItems) {
                Integer quantity = ((Number) item.get("quantity")).intValue();
                Double price = ((Number) item.get("price")).doubleValue();
                dishTotal += quantity * price;
                itemCount += quantity;
            }

            // 计算费用（堂食/自取模式）
            boolean isTakeout = "takeout".equalsIgnoreCase(diningMode);
            double deliveryFee = 0.0;  // 无配送费
            double packagingFee = isTakeout ? itemCount * 2.0 : 0.0;  // 仅自取收取包装费
            double totalAmount = dishTotal + deliveryFee + packagingFee;

            // tableNumber 和 note 已经作为方法参数传入

            // 创建订单
            Order order = new Order();

            // 生成订单ID
            String orderId = IdGenerator.toOrderIdString(IdGenerator.generateId());
            order.setId(orderId);

            order.setUserId(userId);
            order.setMerchantId(merchantId);

            // 设置地址信息（堂食显示座号，自取显示自取标识）
            String addressInfo;
            if (isTakeout) {
                addressInfo = "自取";
            } else {
                addressInfo = "堂食" + (tableNumber != null ? " - 座号：" + tableNumber : "");
            }
            order.setAddress(addressInfo);

            order.setTotalAmount(BigDecimal.valueOf(totalAmount));
            order.setPaidAmount(BigDecimal.ZERO);
            order.setStatus(0);  // 待支付

            // 备注中包含就餐方式和座号信息
            StringBuilder remarkBuilder = new StringBuilder();
            remarkBuilder.append("就餐方式：").append(isTakeout ? "自取" : "堂食");
            if (!isTakeout && tableNumber != null) {
                remarkBuilder.append("，座号：").append(tableNumber);
            }
            if (note != null && !note.isEmpty()) {
                remarkBuilder.append("，备注：").append(note);
            }
            order.setRemark(remarkBuilder.toString());

            // 保存订单
            boolean success = orderService.save(order);

            if (!success) {
                log.error("❌ [Tool] 创建订单失败");
                return "❌ 订单创建失败，请稍后重试";
            }

            // 保存订单菜品
            List<OrderDish> orderDishList = new ArrayList<>();
            for (Map<String, Object> item : dishItems) {
                String dishId = (String) item.get("dishId");
                Integer quantity = ((Number) item.get("quantity")).intValue();
                Double price = ((Number) item.get("price")).doubleValue();

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

            // 批量保存订单菜品
            boolean dishesSaved = orderDishService.saveBatch(orderDishList);

            if (!dishesSaved) {
                log.warn("⚠️ [Tool] 订单菜品保存失败，但订单已创建: {}", orderId);
            }

            // 构建菜品明细文本
            StringBuilder dishesDetail = new StringBuilder();
            dishesDetail.append("📝 菜品明细：\n");
            for (Map<String, Object> item : dishItems) {
                String dishId = (String) item.get("dishId");
                Integer quantity = ((Number) item.get("quantity")).intValue();
                Double price = ((Number) item.get("price")).doubleValue();
                dishesDetail.append(String.format(
                    "  • 菜品ID:%s × %d份 = %.2f元\n",
                    dishId, quantity, price * quantity
                ));
            }

            // 返回待支付状态的订单信息（不说"下单成功"）
            String result = String.format(
                "📋 订单已创建，等待支付\n\n" +
                "🆔 订单号：%s\n" +
                "🏪 商家ID：%s\n" +
                "%s" +
                "🍽️ 就餐方式：%s\n" +
                "%s" +
                "💰 订单金额：%.2f元\n" +
                "📦 包装费：%.2f元\n" +
                "─".repeat(30) + "\n" +
                "💵 **应付总额：%.2f元**\n\n" +
                "💡 请在15分钟内完成支付，超时订单将自动取消\n" +
                "⏰ 预计%s后可取餐",
                orderId,
                merchantId,
                dishesDetail.toString(),
                isTakeout ? "🥡 自取" : "🍽️ 堂食",
                !isTakeout && tableNumber != null ? "🪑 座号：" + tableNumber + "\n" : "",
                dishTotal,
                packagingFee,
                totalAmount,
                isTakeout ? "15-20分钟" : "10-15分钟"
            );

            log.info("✅ [Tool] 订单创建成功: {}, 菜品数: {}", orderId, dishItems.size());
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 创建订单失败", e);
            return "❌ 创建失败：" + e.getMessage();
        }
    }

    /**
     * 查询可用优惠券（简化版）
     *
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @return 可用优惠券信息
     */
    @Tool("""
        查询用户可用的优惠券（简化版）

        **何时使用：**
        - 下单前查询优惠
        - 推荐最优优惠

        **参数：**
        - userId - 用户ID
        - orderAmount - 订单金额

        **返回：** 可用优惠券信息
        """)
    public String getAvailableCoupons(
        @P("用户ID") String userId,
        @P("订单金额") double orderAmount
    ) {
        log.info("🔍 [Tool] 查询可用优惠券，userId: {}, orderAmount: {}", userId, orderAmount);

        // 简化版本：返回固定的优惠券示例
        // 实际应用中需要从数据库查询用户的真实优惠券
        return """
            🎫 可用优惠券

            1. 新用户专享券
               - 满减：满20元减5元
               - 适用：所有商家
               - 有效期：2026-12-31

            2. 午餐优惠券
               - 满减：满30元减8元
               - 适用：所有商家
               - 有效期：2026-06-30

            💡 提示：下单时系统会自动使用最优优惠券
            """;
    }

    /**
     * 准备订单（触发前端显示商家菜品选择卡片）
     *
     * 这个方法用于：
     * 1. 根据商家名称或菜品名称查询商家
     * 2. 获取商家的所有菜品
     * 3. 返回结构化数据，触发前端显示商家菜品卡片
     *
     * @param merchantNameOrDishName 商家名称或菜品名称
     * @param userId 用户ID
     * @return JSON格式的商家菜品数据
     */
    @Tool("""
        准备订单，查询商家和菜品信息（触发前端显示商家菜品选择卡片）

        **何时使用：**
        - 用户说"我想买XX餐厅的..."
        - 用户说"我要下单XX菜品"
        - 用户表达购买意向时

        **参数：**
        - merchantNameOrDishName - 商家名称或菜品名称

        **返回：** 商家菜品卡片数据（JSON格式）
        """)
    public String prepareOrder(
        @P("商家名称或菜品名称") String merchantNameOrDishName,
        @P("用户ID") String userId
    ) {
        log.info("🛒 [Tool] 准备订单，商家/菜品：{}, 用户：{}", merchantNameOrDishName, userId);

        try {
            if (merchantNameOrDishName == null || merchantNameOrDishName.trim().isEmpty()) {
                return "{\"error\": \"请提供商家名称或菜品名称\"}";
            }

            // 1. 先尝试按商家名称查询
            List<Merchant> merchants = merchantService.list().stream()
                .filter(m -> m.getStatus() != null && m.getStatus())
                .filter(m -> m.getName() != null && m.getName().contains(merchantNameOrDishName))
                .collect(Collectors.toList());

            // 2. 如果没找到商家，尝试按菜品名称查询
            if (merchants.isEmpty()) {
                List<Dish> dishes = dishService.list().stream()
                    .filter(d -> d.getName() != null && d.getName().contains(merchantNameOrDishName))
                    .filter(d -> d.getIsOnline() != null && d.getIsOnline())
                    .collect(Collectors.toList());

                if (!dishes.isEmpty()) {
                    // 通过菜品找到商家
                    Set<String> merchantIds = dishes.stream()
                        .map(Dish::getMerchantId)
                        .collect(Collectors.toSet());

                    merchants = merchantService.list().stream()
                        .filter(m -> merchantIds.contains(m.getId()))
                        .filter(m -> m.getStatus() != null && m.getStatus())
                        .collect(Collectors.toList());
                }
            }

            if (merchants.isEmpty()) {
                return "{\"error\": \"未找到相关商家，请确认商家名称或菜品名称\"}";
            }

            // 选择第一个匹配的商家
            Merchant merchant = merchants.get(0);

            // 3. 查询该商家的所有在线菜品
            List<Dish> allDishes = dishService.list().stream()
                .filter(d -> merchant.getId().equals(d.getMerchantId()))
                .filter(d -> d.getIsOnline() != null && d.getIsOnline())
                .collect(Collectors.toList());

            // 4. 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("cardType", "MERCHANT_MENU_CARD");
            result.put("merchant", buildMerchantInfo(merchant));
            result.put("dishes", buildDishesList(allDishes));
            result.put("defaultSelection", findDefaultDishes(allDishes, merchantNameOrDishName));

            String jsonResult = objectMapper.writeValueAsString(result);
            log.info("✅ [Tool] 准备订单成功，商家：{}，菜品数：{}", merchant.getName(), allDishes.size());
            return jsonResult;

        } catch (Exception e) {
            log.error("❌ [Tool] 准备订单失败", e);
            return "{\"error\": \"查询失败：" + e.getMessage() + "\"}";
        }
    }

    /**
     * 构建商家信息
     */
    private Map<String, Object> buildMerchantInfo(Merchant merchant) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", merchant.getId());
        info.put("name", merchant.getName());
        info.put("address", merchant.getAddress());
        info.put("rating", merchant.getRating());
        info.put("averagePrice", merchant.getAveragePrice());
        info.put("status", merchant.getStatus());
        return info;
    }

    /**
     * 构建菜品列表
     */
    private List<Map<String, Object>> buildDishesList(List<Dish> dishes) {
        return dishes.stream().map(dish -> {
            Map<String, Object> d = new HashMap<>();
            d.put("id", dish.getId());
            d.put("name", dish.getName());
            d.put("price", dish.getPrice());
            d.put("calorie", dish.getCalorie());
            d.put("avgRating", dish.getAvgRating());
            d.put("image", dish.getImage());
            d.put("description", dish.getDescription());
            return d;
        }).collect(Collectors.toList());
    }

    /**
     * 根据用户输入查找默认选中的菜品
     */
    private List<Map<String, Object>> findDefaultDishes(List<Dish> dishes, String keyword) {
        return dishes.stream()
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
    }
}
