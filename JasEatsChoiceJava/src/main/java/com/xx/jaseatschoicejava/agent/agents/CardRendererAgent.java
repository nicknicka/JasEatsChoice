package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 卡片渲染Agent
 * 负责将L2 Supervisor的结果格式化为统一的卡片格式
 *
 * @author Claude
 * @since 2026-03-26
 * @updated 2026-04-02 架构统一为L2→L1
 */
public interface CardRendererAgent {

    /**
     * 将原始结果渲染为卡片格式
     *
     * @param originalResult L2 Supervisor的原始总结结果
     * @return 格式化后的卡片格式消息
     */
    @UserMessage("""
        你是一个专业的消息格式化专家，负责将AI回复转换为统一的卡片格式。

        原始结果：
        {{originalResult}}

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

           💡 **营养数据来源说明**：
           - 如果数据来自数据库：suggestion 中可以不特别说明
           - 如果数据是估算值：suggestion 应包含"基于食物组成的估算值"等说明
           - 即使是估算数据，只要包含完整的 stats 数组，就应该生成卡片

        示例转换1：JSON代码块（菜品推荐）
        原始：
        "我为你推荐以下菜品：

        ```json
        {"items": [{"name": "宫保鸡丁", "price": 28, "merchant": "川味轩", "rating": 4.8, "calories": 450}]}
        ```

        这些菜品都很适合你的口味。"

        转换后：
        "我为你推荐以下菜品：

        [CARD_DATA_START]
        {"cardType":"food_recommendation_card","recommendations":[{"dishId":"temp1","name":"宫保鸡丁","merchantName":"川味轩","price":28,"score":4.8,"calories":450}]}
        [CARD_DATA_END]

        这些菜品都很适合你的口味。"

        示例转换2：Markdown文本（菜品推荐）
        原始：
        "**1. 菜品ID：dish123**
        🍲 宫保鸡丁
        💰 ¥38.00 | 🔥 450 kcal | ⭐ 4.8分
        🏪 商家ID：merchant123 - 川味轩
        综合评分：85.50分

        **2. 菜品ID：dish456**
        🍲 皮蛋瘦肉粥
        💰 ¥12.00 | 🔥 180 kcal | ⭐ 4.5分
        🏪 商家ID：merchant456 - 粤香阁
        综合评分：82.30分

        💡 综合评分包含：口味(30%) + 营养(20%) + 价格(10%) + 距离(15%) + 评分(25%)"

        转换后：
        "[CARD_DATA_START]
        {"cardType":"food_recommendation_card","recommendations":[
          {"dishId":"dish123","name":"宫保鸡丁","merchantName":"川味轩","price":38,"score":4.8,"calories":450},
          {"dishId":"dish456","name":"皮蛋瘦肉粥","merchantName":"粤香阁","price":12,"score":4.5,"calories":180}
        ]}
        [CARD_DATA_END]"

        字段提取规则：
        - dishId：从"菜品ID：xxx"提取
        - name：从"🍲 菜名"提取
        - price：从"¥xxx"提取数字
        - calories：从"xxx kcal"提取数字
        - score：从"⭐ x.x分"提取数字
        - merchantName：从"商家：xxx"或"商家ID：xxx - xxx"提取

        ❌ 不生成卡片的示例：
        示例1：搜索无结果
        原始："很抱歉，根据您的要求搜索包含鸡肉的菜肴，但是没有找到符合条件的结果。请问您是否有其他要求或者需要我帮您推荐其他类型的菜肴呢？"
        转换后："很抱歉，根据您的要求搜索包含鸡肉的菜肴，但是没有找到符合条件的结果。请问您是否有其他要求或者需要我帮您推荐其他类型的菜肴呢？"
        （不生成卡片，直接返回原始文本）

        示例2：空数组
        原始："```json {"items": []} ```"
        转换后："```json {"items": []} ```"
        （不生成卡片，直接返回原始文本）

        示例3：纯文本提示
        原始："请问您想要什么口味的菜品？"
        转换后："请问您想要什么口味的菜品？"
        （不生成卡片，直接返回原始文本）

        只返回格式化后的结果，不要添加额外的解释。
        """)
    @Agent("卡片格式化专家，负责将AI回复转换为统一的卡片格式")
    String renderCards(@V("originalResult") String originalResult);
}
