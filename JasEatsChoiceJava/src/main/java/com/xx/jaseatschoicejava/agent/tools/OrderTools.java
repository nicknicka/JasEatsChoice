package com.xx.jaseatschoicejava.agent.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.agent.annotation.CardType;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.UserCoupon;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.UserCouponService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理工具集
 * 使用LangChain4j的@Tool注解声明工具函数
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class OrderTools {

    private static final Logger log = LoggerFactory.getLogger(OrderTools.class);

    @Resource
    private OrderService orderService;

    @Resource
    private DishService dishService;

    @Resource
    private UserCouponService userCouponService;

    /**
     * 创建订单（已废弃，请使用 OrderCreateTools.createOrder）
     *
     * @deprecated 此方法已废弃，请使用 OrderCreateTools.createOrder
     * @param userId 用户ID
     * @param dishIds 菜品ID列表（逗号分隔）
     * @param addressId 地址ID
     * @return 订单信息
     */
    @Tool("⚠️ 此工具已废弃，请使用 createOrder（OrderCreateTools）- 支持堂食/自取模式")
    public String createOrderDeprecated(String userId, String dishIds, String addressId) {
        log.info("执行工具：createOrder，用户：{}，菜品：{}", userId, dishIds);

        try {
            // 解析菜品ID
            List<String> dishIdList = List.of(dishIds.split(","));

            // 获取菜品信息
            List<Dish> dishes = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;
            int totalCalories = 0;

            for (String dishId : dishIdList) {
                Dish dish = dishService.getById(dishId.trim());
                if (dish != null) {
                    dishes.add(dish);
                    if (dish.getPrice() != null) {
                        totalPrice = totalPrice.add(dish.getPrice());
                    }
                    if (dish.getCalorie() != null) {
                        totalCalories += dish.getCalorie();
                    }
                }
            }

            if (dishes.isEmpty()) {
                return "抱歉，未找到指定的菜品，请确认菜品ID是否正确。";
            }

            // 构建订单摘要
            StringBuilder summary = new StringBuilder();
            summary.append("📋 **订单摘要**\n\n");
            summary.append("**菜品清单：**\n");

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                summary.append(String.format("%d. %s - ¥%.2f (%d kcal)\n",
                        i + 1, dish.getName(), dish.getPrice(), dish.getCalorie()));
            }

            summary.append(String.format("\n💰 **总价：** ¥%.2f\n", totalPrice));
            summary.append(String.format("🔥 **总热量：** %d kcal\n\n", totalCalories));
            summary.append("✅ 订单创建成功！厨房正在准备您的美食~");

            return summary.toString();

        } catch (Exception e) {
            log.error("创建订单失败", e);
            return "创建订单失败：" + e.getMessage();
        }
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @Tool("查询订单详情，包括状态、配送信息等")
    public String getOrderDetail(String orderId) {
        log.info("执行工具：getOrderDetail，订单：{}", orderId);

        try {
            Order order = orderService.getById(orderId);

            if (order == null) {
                return String.format("未找到订单 %s，请确认订单ID是否正确。", orderId);
            }

            StringBuilder result = new StringBuilder();
            result.append("📦 **订单详情**\n\n");
            result.append(String.format("**订单号：** %s\n", order.getId()));
            result.append(String.format("**订单状态：** %s\n", getStatusText(order.getStatus())));
            result.append(String.format("**下单时间：** %s\n", order.getCreateTime()));

            if (order.getTotalAmount() != null) {
                result.append(String.format("**订单金额：** ¥%.2f\n", order.getTotalAmount()));
            }

            result.append("\n订单状态说明：");
            result.append("\n- 0：待支付");
            result.append("\n- 1：待接单");
            result.append("\n- 2：制作中");
            result.append("\n- 3：已完成");
            result.append("\n- 4：已取消");

            return result.toString();

        } catch (Exception e) {
            log.error("查询订单失败", e);
            return "查询订单失败：" + e.getMessage();
        }
    }

    /**
     * 获取用户订单列表
     *
     * @param userId 用户ID
     * @return 订单列表（JSON格式的结构化数据）
     */
    @Tool("""
        获取用户的历史订单列表

        **何时使用：**
        - 用户询问"我的订单"、"查看订单"、"订单记录"、"订单列表"
        - 用户想查询订单状态、历史订单、消费记录
        - 用户询问"我买过什么"、"我的消费记录"、"最近订单"

        **参数：**
        - userIdentifier - 用户唯一标识ID

        **返回：** 订单列表，包括订单号、状态、金额、时间
        """)
    public String getUserOrders(
        @P("用户唯一标识ID") String userIdentifier
    ) {
        log.info("执行工具：getUserOrders，用户：{}", userIdentifier);

        try {
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userIdentifier)
                    .orderByDesc("create_time");

            List<Order> orders = orderService.list(queryWrapper);

            if (orders == null || orders.isEmpty()) {
                return "您还没有订单记录，快去下单体验美食吧~";
            }

            // 返回人类可读的文本（给AI看的）
            // 卡片数据会通过AOP在后台自动生成
            StringBuilder result = new StringBuilder();
            result.append("为您查询到 ").append(orders.size()).append(" 条订单记录：\n\n");

            // 显示最近5条订单 - 使用更清晰的格式
            int displayCount = Math.min(5, orders.size());
            for (int i = 0; i < displayCount; i++) {
                Order order = orders.get(i);

                // 使用分隔线区分每个订单
                if (i > 0) {
                    result.append("\n---\n\n");
                }

                // 订单头部
                result.append("**订单").append(i + 1).append("** | ");
                result.append("订单号：`").append(order.getId()).append("`\n\n");

                // 订单详情（使用列表格式）
                result.append("- 状态：").append(getStatusText(order.getStatus())).append("\n");

                if (order.getTotalAmount() != null) {
                    result.append("- 金额：¥").append(String.format("%.2f", order.getTotalAmount())).append("\n");
                }

                if (order.getCreateTime() != null) {
                    // 格式化时间显示
                    String timeStr = order.getCreateTime().toString();
                    if (timeStr.contains("T")) {
                        timeStr = timeStr.substring(0, timeStr.indexOf("T"));
                    }
                    result.append("- 时间：").append(timeStr).append("\n");
                }
            }

            if (orders.size() > 5) {
                result.append("\n\n... 还有 ").append(orders.size() - 5).append(" 条订单，您可以继续查询更多详情");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return "获取订单列表失败：" + e.getMessage();
        }
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @Tool("取消未开始的订单")
    public String cancelOrder(String orderId) {
        log.info("执行工具：cancelOrder，订单：{}", orderId);

        try {
            Order order = orderService.getById(orderId);

            if (order == null) {
                return "未找到该订单";
            }

            // 检查订单状态
            if (order.getStatus() != null && order.getStatus() > 1) {
                return String.format("订单当前状态为「%s」，无法取消。只有待支付或待接单的订单可以取消。", getStatusText(order.getStatus()));
            }

            // 取消订单
            order.setStatus(4); // 4-已取消
            orderService.updateById(order);

            return "✅ 订单已成功取消。如有需要，可以重新下单。";

        } catch (Exception e) {
            log.error("取消订单失败", e);
            return "取消订单失败：" + e.getMessage();
        }
    }

    /**
     * 智能下单辅助工具
     * 根据用户描述搜索菜品，并引导用户前往下单页面
     *
     * @param userId 用户ID
     * @param requirement 用户需求描述
     * @return 下单引导信息
     */
    @Tool("""
        根据用户描述智能搜索菜品，并引导用户前往下单页面

        **何时使用：**
        - 用户说"我要点菜"、"下单"、"1个皮蛋瘦肉粥"、"我要宫保鸡丁"
        - 用户表达购买意图，提到菜品名称

        **参数：**
        - userIdentifier - 用户唯一标识ID
        - requirement - 用户需求描述，例如："1个皮蛋瘦肉粥"、"我要宫保鸡丁和鱼香肉丝"

        **返回：** 找到的菜品信息，引导用户前往下单页面确认并支付
        """)
    @com.xx.jaseatschoicejava.agent.annotation.CardType(value = "order_guide_card", priority = 10)
    public String smartOrder(
        @P("用户唯一标识ID") String userIdentifier,
        @P("用户需求描述，如：1个皮蛋瘦肉粥") String requirement
    ) {
        log.info("执行工具：smartOrder，用户：{}，需求：{}", userIdentifier, requirement);

        try {
            // 1. 解析用户需求，提取菜品名称和数量
            List<String> dishNames = extractDishNames(requirement);
            List<Integer> quantities = extractQuantities(requirement, dishNames.size());

            if (dishNames.isEmpty()) {
                return "🤖 **智能下单助手**\n\n"
                    + "我理解您想下单，但没听清楚您要什么菜品。\n\n"
                    + "请告诉我您想点的菜名，例如：\n"
                    + "- \"1个皮蛋瘦肉粥\"\n"
                    + "- \"我要宫保鸡丁和鱼香肉丝\"\n"
                    + "- \"来一份西红柿鸡蛋汤\"";
            }

            // 2. 搜索菜品数据库
            List<Dish> foundDishes = new ArrayList<>();
            List<Integer> finalQuantities = new ArrayList<>();
            StringBuilder notFoundDishes = new StringBuilder();

            for (int i = 0; i < dishNames.size(); i++) {
                String dishName = dishNames.get(i);

                // 模糊搜索菜品
                QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
                queryWrapper.like("name", dishName)
                        .eq("is_online", true);  // 只搜索上架的菜品

                List<Dish> dishes = dishService.list(queryWrapper);

                if (dishes.isEmpty()) {
                    notFoundDishes.append(String.format("- %s\n", dishName));
                } else {
                    // 取第一个匹配的菜品
                    Dish dish = dishes.get(0);
                    foundDishes.add(dish);
                    finalQuantities.add(quantities.get(i));
                }
            }

            // 检查是否有未找到的菜品
            if (notFoundDishes.length() > 0 && foundDishes.isEmpty()) {
                return String.format("❌ **菜品未找到**\n\n"
                    + "抱歉，以下菜品没有找到：\n\n"
                    + "%s"
                    + "\n可能原因：\n"
                    + "- 菜品名称不正确\n"
                    + "- 菜品已下架\n"
                    + "- 该商家暂未营业\n\n"
                    + "您可以换个菜名试试，或者使用「推荐美食」功能查看可用菜品。", notFoundDishes);
            }

            // 3. 构建菜品清单，引导用户下单
            StringBuilder result = new StringBuilder();
            result.append("✅ **已为您找到以下菜品**\n\n");

            // 构建菜品信息
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalCalories = 0;

            for (int i = 0; i < foundDishes.size(); i++) {
                Dish dish = foundDishes.get(i);
                Integer quantity = finalQuantities.get(i);

                BigDecimal subtotal = dish.getPrice().multiply(new BigDecimal(quantity));
                totalAmount = totalAmount.add(subtotal);

                if (dish.getCalorie() != null) {
                    totalCalories += dish.getCalorie() * quantity;
                }

                result.append(String.format("**%d. %s**\n", i + 1, dish.getName()));
                result.append(String.format("   - 价格：¥%.2f × %d = ¥%.2f\n",
                    dish.getPrice(), quantity, subtotal));
                if (dish.getCalorie() != null) {
                    result.append(String.format("   - 热量：%d kcal\n", dish.getCalorie() * quantity));
                }
                if (dish.getMerchantId() != null) {
                    result.append(String.format("   - 商家ID：%s\n", dish.getMerchantId()));
                }
                result.append(String.format("   - 菜品ID：%s\n", dish.getId()));
                result.append("\n");
            }

            if (notFoundDishes.length() > 0) {
                result.append("**⚠️ 以下菜品未找到：**\n");
                result.append(notFoundDishes);
                result.append("\n");
            }

            result.append(String.format("**💰 预计总价：¥%.2f**\n", totalAmount));
            result.append(String.format("**🔥 总热量：%d kcal**\n\n", totalCalories));

            // 4. 引导用户前往下单页面
            result.append("**📱 下一步操作：**\n\n");
            result.append("请前往「下单页面」完成订单：\n");
            result.append("1. 确认配送地址\n");
            result.append("2. 选择支付方式\n");
            result.append("3. 确认并支付订单\n\n");

            result.append("💡 **提示：**\n");
            result.append("- 您可以在下单页面调整菜品数量\n");
            result.append("- 如需添加备注，请在下单页面填写\n");
            result.append("- 支持使用钱包余额或优惠券支付");

            return result.toString();

        } catch (Exception e) {
            log.error("智能下单失败", e);
            return "❌ 智能下单失败：" + e.getMessage();
        }
    }

    /**
     * 从用户需求中提取菜品名称
     * 例如："1个皮蛋瘦肉粥和2份宫保鸡丁" -> ["皮蛋瘦肉粥", "宫保鸡丁"]
     */
    private List<String> extractDishNames(String requirement) {
        List<String> dishNames = new ArrayList<>();

        // 常见的量词
        String[] quantityPatterns = {"\\d+个", "\\d+份", "\\d+碗", "\\d+盘",
                                     "\\d+\\s*个", "\\d+\\s*份", "\\d+\\s*碗", "\\d+\\s*盘"};

        // 先移除数量词，提取菜品名
        String cleaned = requirement;
        for (String pattern : quantityPatterns) {
            cleaned = cleaned.replaceAll(pattern, "");
        }

        // 移除常见词汇
        cleaned = cleaned.replaceAll("[我要想吃来份个碗盘]+", "");
        cleaned = cleaned.replaceAll("[和，,、]+", " ");

        // 分割菜品名
        String[] parts = cleaned.trim().split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty() && part.length() >= 2) {  // 菜名至少2个字
                dishNames.add(part);
            }
        }

        return dishNames;
    }

    /**
     * 从用户需求中提取数量
     * 例如："1个皮蛋瘦肉粥和2份宫保鸡丁" -> [1, 2]
     */
    private List<Integer> extractQuantities(String requirement, int dishCount) {
        List<Integer> quantities = new ArrayList<>();

        // 提取所有数字
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)[个份碗盘]");
        java.util.regex.Matcher matcher = pattern.matcher(requirement);

        while (matcher.find()) {
            quantities.add(Integer.parseInt(matcher.group(1)));
        }

        // 如果没有明确数量，默认为1
        while (quantities.size() < dishCount) {
            quantities.add(1);
        }

        return quantities;
    }

    /**
     * 计算订单预估时间
     *
     * @param dishIds 菜品ID列表
     * @return 预估时间
     */
    @Tool("计算订单预计完成时间")
    public String estimateOrderTime(String dishIds) {
        log.info("执行工具：estimateOrderTime，菜品：{}", dishIds);

        try {
            List<String> dishIdList = List.of(dishIds.split(","));
            int totalMinutes = 0;

            for (String dishId : dishIdList) {
                Dish dish = dishService.getById(dishId.trim());
                if (dish != null && dish.getEstimatedCookingMinutes() != null) {
                    totalMinutes += dish.getEstimatedCookingMinutes();
                }
            }

            // 基础准备时间
            totalMinutes += 10;
            // 配送时间（假设30分钟）
            totalMinutes += 30;

            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            StringBuilder result = new StringBuilder();
            result.append("⏰ **预计配送时间**\n\n");

            if (hours > 0) {
                result.append(String.format("预计 %d小时%d分钟后送达", hours, minutes));
            } else {
                result.append(String.format("预计 %d分钟后送达", minutes));
            }

            result.append("\n\n（实际送达时间可能因交通、天气等因素有所变化）");

            return result.toString();

        } catch (Exception e) {
            log.error("计算预估时间失败", e);
            return "计算预估时间失败：" + e.getMessage();
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待接单";
            case 2 -> "制作中";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    /**
     * 推荐最优优惠券
     * 根据订单金额选择最优惠的优惠券
     *
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @return 推荐的优惠券信息
     */
    @Tool("根据订单金额推荐用户可用的最优优惠券，自动选择省得最多的")
    public String recommendBestCoupon(String userId, BigDecimal orderAmount) {
        log.info("执行工具：recommendBestCoupon，用户：{}，金额：{}", userId, orderAmount);

        try {
            // 1. 获取用户所有可用优惠券
            List<UserCoupon> availableCoupons = userCouponService.getAvailableCoupons(userId);

            if (availableCoupons == null || availableCoupons.isEmpty()) {
                return "💳 **优惠券查询**\n\n您暂时没有可用的优惠券。";
            }

            // 2. 筛选满足最低消费金额的优惠券
            LocalDateTime now = LocalDateTime.now();
            List<UserCoupon> validCoupons = availableCoupons.stream()
                    .filter(coupon -> coupon.getMinAmount() == null ||
                                    orderAmount.compareTo(coupon.getMinAmount()) >= 0)
                    .filter(coupon -> coupon.getExpireTime() == null ||
                                    coupon.getExpireTime().isAfter(now))
                    .collect(Collectors.toList());

            if (validCoupons.isEmpty()) {
                StringBuilder result = new StringBuilder();
                result.append("💳 **优惠券查询**\n\n");
                result.append(String.format("您的优惠券暂不满足使用条件（订单金额：¥%.2f）。\n\n", orderAmount));
                result.append("可用优惠券：\n");

                for (UserCoupon coupon : availableCoupons) {
                    result.append(String.format("- %s：减免¥%s（满¥%s可用）\n",
                            coupon.getName(),
                            coupon.getAmount(),
                            coupon.getMinAmount()));
                }

                return result.toString();
            }

            // 3. 选择最优优惠券（优惠金额最大，如果相同则选择即将过期的）
            UserCoupon bestCoupon = validCoupons.stream()
                    .max(Comparator.comparing(UserCoupon::getAmount)
                            .thenComparing(UserCoupon::getExpireTime))
                    .orElse(null);

            if (bestCoupon == null) {
                return "优惠券推荐失败，请稍后再试。";
            }

            // 4. 构建推荐结果
            BigDecimal finalAmount = orderAmount.subtract(bestCoupon.getAmount());
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            StringBuilder result = new StringBuilder();
            result.append("🎟️ **最优优惠券推荐**\n\n");
            result.append(String.format("**优惠券名称：** %s\n", bestCoupon.getName()));
            result.append(String.format("**优惠金额：** ¥%s\n", bestCoupon.getAmount()));
            result.append(String.format("**原订单金额：** ¥%.2f\n", orderAmount));
            result.append(String.format("**优惠后金额：** ¥%.2f\n", finalAmount));
            result.append(String.format("**节省：** ¥%.2f\n", orderAmount.subtract(finalAmount)));

            if (bestCoupon.getExpireTime() != null) {
                result.append(String.format("**有效期至：** %s\n", bestCoupon.getExpireTime()));
            }

            result.append("\n✅ 已为您自动选择最优优惠券！");

            return result.toString();

        } catch (Exception e) {
            log.error("推荐优惠券失败", e);
            return "推荐优惠券失败：" + e.getMessage();
        }
    }

    /**
     * 查询可用优惠券列表
     *
     * @param userId 用户ID
     * @return 可用优惠券列表
     */
    @Tool("查询用户所有可用的优惠券列表")
    public String getAvailableCoupons(String userId) {
        log.info("执行工具：getAvailableCoupons，用户：{}", userId);

        try {
            List<UserCoupon> availableCoupons = userCouponService.getAvailableCoupons(userId);

            if (availableCoupons == null || availableCoupons.isEmpty()) {
                return "💳 **我的优惠券**\n\n您暂时没有可用的优惠券。";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("💳 **我的优惠券**（共%d张）\n\n", availableCoupons.size()));

            for (int i = 0; i < availableCoupons.size(); i++) {
                UserCoupon coupon = availableCoupons.get(i);
                result.append(String.format("%d. %s\n", i + 1, coupon.getName()));
                result.append(String.format("   优惠：¥%s", coupon.getAmount()));

                if (coupon.getMinAmount() != null && coupon.getMinAmount().compareTo(BigDecimal.ZERO) > 0) {
                    result.append(String.format("（满¥%s可用）", coupon.getMinAmount()));
                }

                if (coupon.getExpireTime() != null) {
                    result.append(String.format("\n   有效期至：%s", coupon.getExpireTime()));
                }

                result.append("\n\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("查询优惠券失败", e);
            return "查询优惠券失败：" + e.getMessage();
        }
    }
}
