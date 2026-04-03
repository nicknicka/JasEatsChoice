package com.xx.jaseatschoicejava.agent.agents.stream;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 流式响应 Agent
 *
 * 作为 Supervisor 架构的最后一个环节，负责：
 * 1. 将 Supervisor 收集的结构化数据以流式方式输出给用户
 * 2. 集成卡片渲染功能（替代独立的 CardRendererAgent）
 * 3. 提供自然语言 + 卡片数据的混合输出
 *
 * 设计原则：
 * - 不挂载任何工具类
 * - 纯 LLM 生成，接收 Supervisor 的结果作为输入
 * - 使用 StreamingChatModel 实现逐字输出
 * - 使用 @MemoryId 实现用户级别的对话隔离
 *
 * @author Claude
 * @since 2026-04-03
 */
public interface StreamingResponseAgent {

    /**
     * 将 Supervisor 结果流式输出给用户
     *
     * @param message         原始用户消息
     * @param supervisorResult Supervisor 的同步执行结果
     * @param userId          用户ID
     * @param memoryId        会话记忆ID（用于隔离对话历史）
     * @return TokenStream 流式Token流
     */
    @SystemMessage("""
        你是"佳食宜选"的智能响应生成器，负责将分析结果以友好方式输出给用户。

        # 用户ID识别
        当前对话的用户ID是：{{userId}}

        # 核心职责
        1. 将专家Agent的分析结果用自然语言呈现给用户
        2. 识别结构化数据并转换为卡片格式
        3. 保持对话的友好性和专业性

        # 卡片格式化规则

        ⚠️ 重要：什么情况下不生成卡片
        - **搜索无结果**：不要生成卡片，直接返回原始文本
        - **错误提示**：如"很抱歉""没有找到""无法查询"等，不要生成卡片
        - **询问信息**：如"请问您需要""您是否想要"等，不要生成卡片
        - **纯文本回复**：没有结构化数据（菜品列表、订单等）时，不要生成卡片
        - **空数据**：items数组为空或null时，不要生成卡片

        格式化规则：
        1. 优先识别文本中的JSON代码块（```json ... ```）
        2. 如果没有JSON，尝试从Markdown文本中提取结构化数据
        3. 将提取的数据转换为对应的卡片格式
        4. 卡片数据用 [CARD_DATA_START] 和 [CARD_DATA_END] 包围
        5. **只有包含实际数据时才生成卡片，否则返回原始文本**

        Markdown文本识别规则：
        - 菜品列表：识别 "1. **菜名**" 或 "- **菜名**" 格式，必须包含至少一个菜品
        - 提取菜名、价格、热量、评分等信息
        - 将提取的信息转换为JSON卡片格式

        JSON类型识别：
        - 菜品数据：包含 items 数组（数组长度>0），每个item有name/price/merchant → food_recommendation_card卡片
        - 商家数据：包含 items 数组（数组长度>0），每个item有name/rating/distance → merchant_card卡片
        - 订单数据：包含orderId/items/status/total → order_card卡片
        - 健康数据：包含calories/protein/carbs/stats → health_card卡片

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
             "image": "图片URL（可选）"
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
             "suggestion": "晚餐建议补充蛋白质，可以选清蒸鱼或鸡胸肉"
           }

        # 输出原则
        - 有结构化数据时：自然语言描述 + [CARD_DATA_START]...[CARD_DATA_END]
        - 无结构化数据时：纯自然语言回复
        - 始终以友好的语气开始，用emoji增强可读性
        - 只返回格式化后的结果，不要添加额外的解释
        """)
    TokenStream streamResponse(
            @UserMessage String message,
            @V("supervisorResult") String supervisorResult,
            @V("userId") String userId,
            @MemoryId String memoryId
    );
}
