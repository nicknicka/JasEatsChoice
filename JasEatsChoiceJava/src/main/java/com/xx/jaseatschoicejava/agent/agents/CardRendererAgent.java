package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 卡片渲染Agent
 * 负责将L3 Supervisor的结果格式化为统一的卡片格式
 *
 * @author Claude
 * @since 2026-03-26
 */
public interface CardRendererAgent {

    /**
     * 将原始结果渲染为卡片格式
     *
     * @param originalResult L3 Supervisor的原始总结结果
     * @return 格式化后的卡片格式消息
     */
    @UserMessage("""
        你是一个专业的消息格式化专家，负责将AI回复转换为统一的卡片格式。

        原始结果：
        {{originalResult}}

        格式化规则：
        1. 识别文本中的JSON代码块（```json ... ```）
        2. 将JSON数据转换为对应的卡片格式
        3. 保留JSON前后的自然语言文本
        4. 多个卡片用 <!-- CARD_LIST_START --> 和 <!-- CARD_LIST_END --> 包围

        JSON类型识别：
        - 菜品数据：包含 items 数组，每个item有name/price/merchant → dish卡片
        - 商家数据：包含 items 数组，每个item有name/rating/distance → merchant卡片
        - 订单数据：包含orderId/items/status/total → order卡片
        - 健康数据：包含calories/protein/carbs/stats → health卡片

        卡片格式定义：
        1. 菜品卡片：
           {
             "type": "dish",
             "title": "宫保鸡丁",
             "subtitle": "川味轩 ⭐ 4.8",
             "tags": ["辣", "推荐"],
             "price": 28,
             "rating": 4.8,
             "image": "图片URL（可选）",
             "actions": [{"text": "加订单", "type": "primary"}, {"text": "详情", "type": "default"}],
             "highlight": "符合你的口味偏好"
           }

        2. 商家卡片：
           {
             "type": "merchant",
             "title": "川味轩",
             "subtitle": "川菜 · 配送中",
             "tags": ["4.8分", "月售1000+"],
             "info": {
               "distance": "1.2km",
               "deliveryTime": "30分钟",
               "deliveryFee": "¥5"
             },
             "image": "图片URL（可选）",
             "actions": [{"text": "查看菜单", "type": "primary"}]
           }

        3. 订单卡片：
           {
             "type": "order",
             "title": "订单 #2024032612345",
             "subtitle": "川味轩",
             "status": "配送中",
             "statusColor": "orange",
             "items": [
               {"name": "宫保鸡丁", "quantity": 1, "price": 28},
               {"name": "米饭", "quantity": 2, "price": 4}
             ],
             "total": 32,
             "actions": [{"text": "查看详情", "type": "primary"}],
             "timeline": [
               {"time": "12:30", "event": "订单已创建"},
               {"time": "12:35", "event": "商家已接单"}
             ]
           }

        4. 健康卡片：
           {
             "type": "health",
             "title": "今日营养分析",
             "subtitle": "2024-03-26",
             "stats": [
               {"label": "卡路里", "value": "1450/1800", "percent": 80, "color": "green"},
               {"label": "蛋白质", "value": "65g/80g", "percent": 81, "color": "blue"},
               {"label": "碳水", "value": "180g/250g", "percent": 72, "color": "orange"}
             ],
             "suggestion": "晚餐建议补充蛋白质，可以选清蒸鱼或鸡胸肉",
             "actions": [{"text": "查看详情", "type": "primary"}]
           }

        示例转换：
        原始：
        "我为你推荐以下菜品：

        ```json
        {"items": [{"name": "宫保鸡丁", "price": 28, "merchant": "川味轩", "rating": 4.8}]}
        ```

        这些菜品都很适合你的口味。"

        转换后：
        "我为你推荐以下菜品：

        <!-- CARD_LIST_START -->
        <!-- CARD_START:dish -->
        {"type":"dish","title":"宫保鸡丁","subtitle":"川味轩 ⭐ 4.8","tags":["推荐"],"price":28,"rating":4.8,"actions":[{"text":"加订单","type":"primary"}]}
        <!-- CARD_END -->
        <!-- CARD_LIST_END -->

        这些菜品都很适合你的口味。"

        只返回格式化后的结果，不要添加额外的解释。
        """)
    @Agent("卡片格式化专家，负责将AI回复转换为统一的卡片格式")
    String renderCards(@V("originalResult") String originalResult);
}
