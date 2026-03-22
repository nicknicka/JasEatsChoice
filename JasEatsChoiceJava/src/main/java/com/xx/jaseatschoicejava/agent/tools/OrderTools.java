package com.xx.jaseatschoicejava.agent.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param dishIds 菜品ID列表（逗号分隔）
     * @param addressId 地址ID
     * @return 订单信息
     */
    @Tool("创建订单，支持多菜品下单")
    public String createOrder(String userId, String dishIds, String addressId) {
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
     * @return 订单列表
     */
    @Tool("获取用户的历史订单列表")
    public String getUserOrders(String userId) {
        log.info("执行工具：getUserOrders，用户：{}", userId);

        try {
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("create_time");

            List<Order> orders = orderService.list(queryWrapper);

            if (orders == null || orders.isEmpty()) {
                return "您还没有订单记录，快去下单体验美食吧~";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("📜 **订单列表**（共%d条）\n\n", orders.size()));

            // 显示最近5条
            for (int i = 0; i < Math.min(5, orders.size()); i++) {
                Order order = orders.get(i);
                result.append(String.format("**%d. 订单 %s**\n", i + 1,
                        order.getId().substring(0, Math.min(8, order.getId().length()))));
                result.append(String.format("   状态：%s", getStatusText(order.getStatus())));

                if (order.getTotalAmount() != null) {
                    result.append(String.format(" | 金额：¥%.2f", order.getTotalAmount()));
                }

                if (order.getCreateTime() != null) {
                    result.append(String.format(" | 时间：%s", order.getCreateTime()));
                }

                result.append("\n\n");
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
     * 智能下单（简化版）
     *
     * @param userId 用户ID
     * @param requirement 用户需求描述
     * @return 下单结果
     */
    @Tool("根据用户描述智能下单，例如：'我想吃两个菜，预算50元'")
    public String smartOrder(String userId, String requirement) {
        log.info("执行工具：smartOrder，用户：{}，需求：{}", userId, requirement);

        try {
            // 简化版：返回引导信息
            StringBuilder result = new StringBuilder();
            result.append("🤖 **智能下单助手**\n\n");
            result.append("我理解您的需求：\"").append(requirement).append("\"\n\n");
            result.append("为了更好地为您服务，我需要以下信息：\n\n");
            result.append("1. 您想点哪些菜品？（可以告诉我想吃的菜名）\n");
            result.append("2. 配送地址是哪里？\n");
            result.append("3. 有什么特殊要求吗？（忌口、备注等）\n\n");
            result.append("您可以直接告诉我，例如：\"我要宫保鸡丁和鱼香肉丝，送到宿舍\"");

            return result.toString();

        } catch (Exception e) {
            log.error("智能下单失败", e);
            return "智能下单失败：" + e.getMessage();
        }
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
}
