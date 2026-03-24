package com.xx.jaseatschoicejava.agent.tools.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.OrderService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
        - 配送费（固定5元）
        - 包装费（每项2元）
        - 最终总计

        **何时使用：**
        - 下单前确认价格
        - 比较不同方案

        **参数：**
        - dishItemsJson - 菜品列表（JSON格式）
          例如：[{"dishId":"xxx","quantity":2,"price":15.5}]
        - userId - 用户ID

        **返回：** 价格明细（文本格式）
        """)
    public String calculateOrderPrice(
        @P("菜品列表（JSON格式）") String dishItemsJson,
        @P("用户ID") String userId
    ) {
        log.info("🔍 [Tool] 计算订单价格，userId: {}", userId);

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
            double deliveryFee = 5.0;  // 固定配送费
            double packagingFee = itemCount * 2.0;  // 每项2元包装费
            double total = dishTotal + deliveryFee + packagingFee;

            String result = String.format(
                "💰 订单价格明细\n\n" +
                "%s" +
                "\n🍱 菜品小计：%.2f元\n" +
                "📦 配送费：%.2f元\n" +
                "🎁 包装费：%.2f元\n" +
                "\n" +
                "─".repeat(30) + "\n" +
                "💵 **总计：%.2f元**\n\n" +
                "💡 下单时如有优惠券，系统会自动抵扣",
                itemsDetail.toString(),
                dishTotal,
                deliveryFee,
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
     * @param orderRequestJson 订单请求JSON
     * @return 订单创建结果
     */
    @Tool("""
        创建一个新的订单

        **必需参数：**
        - userId: 用户ID
        - merchantId: 商家ID
        - dishItems: 菜品列表（JSON格式）
        - deliveryAddress: 配送地址
        - phoneNumber: 联系电话

        **可选参数：**
        - note: 备注信息

        **输入示例：**
        {
          "userId": "U1234567890123456",
          "merchantId": "M9876543210987654",
          "dishItems": [
            {"dishId": "D001", "quantity": 2, "price": 15.5}
          ],
          "deliveryAddress": "XX大学XX宿舍",
          "phoneNumber": "13800138000",
          "note": "少辣"
        }

        **何时使用：**
        - 用户明确要下单
        - 确认订单信息后创建

        **参数：** orderRequestJson - 订单信息（JSON格式）

        **返回：** 订单创建结果
        """)
    public String createOrder(
        @P("订单信息（JSON格式）") String orderRequestJson
    ) {
        log.info("🔍 [Tool] 创建订单");

        try {
            Map<String, Object> request = objectMapper.readValue(
                orderRequestJson,
                new TypeReference<Map<String, Object>>() {}
            );

            // 验证必需参数
            String userId = (String) request.get("userId");
            String merchantId = (String) request.get("merchantId");
            String deliveryAddress = (String) request.get("deliveryAddress");
            String phoneNumber = (String) request.get("phoneNumber");

            if (userId == null || merchantId == null || deliveryAddress == null || phoneNumber == null) {
                return "❌ 缺少必需参数，需要：userId、merchantId、deliveryAddress、phoneNumber";
            }

            // 计算订单金额
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dishItems = (List<Map<String, Object>>) request.get("dishItems");
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

            double deliveryFee = 5.0;
            double packagingFee = itemCount * 2.0;
            double totalAmount = dishTotal + deliveryFee + packagingFee;

            // 创建订单
            Order order = new Order();
            order.setUserId(userId);
            order.setMerchantId(merchantId);
            order.setAddress(deliveryAddress);
            order.setTotalAmount(BigDecimal.valueOf(totalAmount));
            order.setPaidAmount(BigDecimal.ZERO);
            order.setStatus(0);  // 待支付
            order.setRemark((String) request.get("note"));

            boolean success = orderService.save(order);

            if (success) {
                String result = String.format(
                    "✅ 订单创建成功！\n\n" +
                    "📋 订单号：%s\n" +
                    "🏪 商家ID：%s\n" +
                    "💰 订单金额：%.2f元\n" +
                    "📍 配送地址：%s\n" +
                    "📞 联系电话：%s\n" +
                    "📝 备注：%s\n\n" +
                    "💡 请及时支付订单，超时将自动取消",
                    order.getId(),
                    merchantId,
                    totalAmount,
                    deliveryAddress,
                    phoneNumber,
                    order.getRemark() != null ? order.getRemark() : "无"
                );

                log.info("✅ [Tool] 创建订单成功: {}", order.getId());
                return result;
            } else {
                log.error("❌ [Tool] 创建订单失败");
                return "❌ 订单创建失败，请稍后重试";
            }

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
}
