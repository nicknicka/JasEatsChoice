package com.xx.jaseatschoicejava.agent.tools.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.OrderService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agentic.scope.AgenticScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 订单查询工具类
 *
 * 为Agent提供订单信息的查询功能
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class OrderQueryTools {

    @Resource
    private OrderService orderService;

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情（文本格式）
     */
    @Tool("""
        查询订单的详细信息

        **返回信息：**
        - 订单号
        - 订单状态
        - 商家信息
        - 价格明细
        - 就餐信息（堂食/自取）
        - 下单时间

        **何时使用：**
        - 用户查询订单
        - 订单状态跟踪
        - 订单详情查看

        **参数：** orderId - 订单ID

        **返回：** 订单详情（文本格式）
        """)
    public String getOrderDetail(
        @P("订单ID") String orderId
    ) {
        log.info("🔍 [Tool] 查询订单详情，orderId: {}", orderId);

        try {
            Order order = orderService.getById(orderId);

            if (order == null) {
                log.warn("❌ [Tool] 订单不存在，orderId: {}", orderId);
                return "❌ 订单不存在";
            }

            String result = String.format(
                "✅ 订单详情\n\n" +
                "📋 订单号：%s\n" +
                "📊 订单状态：%s\n" +
                "🏪 商家ID：%s\n" +
                "💰 订单金额：%.2f元\n" +
                "💵 已支付：%.2f元\n" +
                "🍽️ 就餐信息：%s\n" +
                "📝 备注：%s\n" +
                "📅 下单时间：%s",
                order.getId(),
                getStatusText(order.getStatus()),
                order.getMerchantId(),
                order.getTotalAmount(),
                order.getPaidAmount(),
                order.getAddress() != null ? order.getAddress() : "无",
                order.getRemark() != null ? order.getRemark() : "无",
                order.getCreateTime()
            );

            log.info("✅ [Tool] 查询订单详情成功: {}", order.getId());
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询订单详情失败，orderId: {}", orderId, e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 查询用户所有订单
     *
     * @param userId 用户ID
     * @return 订单列表摘要
     */
    @Tool("""
        查询当前用户的所有订单（最近20条）

        **何时使用：**
        - 用户查看历史订单
        - 订单管理
        - 订单统计

        **无需参数**，userId自动从上下文获取

        **返回：** 订单列表摘要
        """)
    public String getUserOrders(AgenticScope scope) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 查询用户订单列表，userId: {}", userId);

        try {
            List<Order> orders = orderService.list(
                new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, userId)
                    .orderByDesc(Order::getCreateTime)
                    .last("LIMIT 20")
            );

            if (orders.isEmpty()) {
                return "📋 您还没有订单";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📋 您的订单列表（最近20条）\n\n");

            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                sb.append(String.format(
                    "%d. 订单号：%s\n" +
                    "   状态：%s | 金额：%.2f元\n" +
                    "   时间：%s\n\n",
                    i + 1,
                    order.getId(),
                    getStatusText(order.getStatus()),
                    order.getTotalAmount(),
                    order.getCreateTime()
                ));
            }

            log.info("✅ [Tool] 查询用户订单成功，数量: {}", orders.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询用户订单失败，userId: {}", userId, e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 查询订单状态
     *
     * @param orderId 订单ID
     * @return 订单状态信息
     */
    @Tool("""
        查询订单的当前状态

        **订单状态：**
        - 0：待支付
        - 1：待接单
        - 2：制作中
        - 3：已完成
        - 4：已取消

        **何时使用：**
        - 快速查询订单状态
        - 配送跟踪
        - 订单进度查询

        **参数：** orderId - 订单ID

        **返回：** 订单状态信息
        """)
    public String getOrderStatus(
        @P("订单ID") String orderId
    ) {
        log.info("🔍 [Tool] 查询订单状态，orderId: {}", orderId);

        try {
            Order order = orderService.getById(orderId);

            if (order == null) {
                return "❌ 订单不存在";
            }

            String statusText = getStatusText(order.getStatus());
            String result = String.format(
                "✅ 订单状态查询结果\n\n" +
                "📋 订单号：%s\n" +
                "📊 当前状态：%s\n" +
                "📅 下单时间：%s\n" +
                "💰 订单金额：%.2f元",
                order.getId(),
                statusText,
                order.getCreateTime(),
                order.getTotalAmount()
            );

            log.info("✅ [Tool] 查询订单状态成功: {}", statusText);
            return result;

        } catch (Exception e) {
            log.error("❌ [Tool] 查询订单状态失败，orderId: {}", orderId, e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 获取推荐地址
     *
     * @param userId 用户ID
     * @return 推荐地址列表
     */
    @Tool("""
        获取当前用户的推荐配送地址

        **推荐地址基于：**
        - 历史订单地址
        - 使用频率
        - 最近使用时间

        **何时使用：**
        - 创建订单时智能填充
        - 用户询问"送到哪里"

        **无需参数**，userId自动从上下文获取

        **返回：** 推荐地址列表（按优先级排序）
        """)
    public String getRecommendedAddress(AgenticScope scope) {
        String userId = (String) scope.readState("userId");
        if (userId == null || userId.isEmpty()) {
            return "❌ 无法获取用户信息，请重新登录";
        }
        log.info("🔍 [Tool] 查询推荐地址，userId: {}", userId);

        try {
            // 查询用户最近的订单，获取地址
            List<Order> orders = orderService.list(
                new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, userId)
                    .isNotNull(Order::getAddress)
                    .ne(Order::getAddress, "")
                    .orderByDesc(Order::getCreateTime)
                    .last("LIMIT 5")
            );

            if (orders.isEmpty()) {
                return "📍 暂无历史配送地址";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📍 推荐配送地址（基于历史订单）\n\n");

            int count = 0;
            for (Order order : orders) {
                if (order.getAddress() != null && !order.getAddress().isEmpty()) {
                    count++;
                    sb.append(String.format("%d. %s\n", count, order.getAddress()));
                }
            }

            log.info("✅ [Tool] 查询推荐地址成功，数量: {}", count);
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ [Tool] 查询推荐地址失败，userId: {}", userId, e);
            return "❌ 查询失败：" + e.getMessage();
        }
    }

    /**
     * 将订单状态码转换为文本
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
