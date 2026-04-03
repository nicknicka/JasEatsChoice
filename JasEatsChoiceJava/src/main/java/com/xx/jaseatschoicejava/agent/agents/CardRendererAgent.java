package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 卡片渲染Agent（UniCard Schema 输出格式）
 *
 * 负责将L2 Supervisor的结果格式化为统一的 UniCard Schema 格式。
 * 所有卡片遵循统一的 {schema, header, elements[], actions[]} 结构，
 * 前端根据 schema 字段识别版本，根据 element.tag 动态选择渲染组件。
 *
 * @author Claude
 * @since 2026-03-26
 * @updated 2026-04-03 改造为 UniCard Schema 输出格式
 */
public interface CardRendererAgent {

    /**
     * 将原始结果渲染为 UniCard Schema 卡片格式
     *
     * @param originalResult L2 Supervisor的原始总结结果
     * @return 格式化后的卡片格式消息
     */
    @UserMessage("""
        你是一个专业的消息格式化专家，负责将AI回复转换为统一的 UniCard Schema 卡片格式。

        原始结果：
        {{originalResult}}

        # UniCard Schema 定义

        所有卡片使用统一的结构，前端根据 `schema` 字段识别版本，根据 `elements[].tag` 动态选择渲染组件。

        ```json
        {
          "schema": "jaseat_card_v1",
          "header": {
            "title": { "text": "标题文字", "icon": "emoji图标" },
            "subtitle": "副标题描述",
            "theme": "dish|merchant|order|health"
          },
          "elements": [ ... ],
          "actions": [ ... ],
          "footer": { "note": "底部备注" },
          "displayMode": "inline"
        }
        ```

        # 何时生成卡片

        **不生成卡片的情况（直接返回原始文本）：**
        - 搜索无结果（"没有找到""很抱歉"）
        - 错误提示
        - 询问信息（"请问您需要""您是否想要"）
        - 纯文本回复，无结构化数据
        - 空数据（items数组为空或null）

        **生成卡片的情况：**
        - 包含菜品列表数据
        - 包含商家列表数据
        - 包含订单数据
        - 包含营养/健康统计数据
        - 包含可操作的结构化信息

        # 数据识别规则

        1. 优先识别文本中的JSON代码块（```json ... ```）
        2. 如果没有JSON，从Markdown文本中提取结构化数据
        3. Markdown菜品列表：识别 "1. **菜名**" 或 "- **菜名**" 格式

        # Theme 映射

        - 菜品/美食相关 → `"theme": "dish"`
        - 商家/店铺相关 → `"theme": "merchant"`
        - 订单相关 → `"theme": "order"`
        - 营养/健康相关 → `"theme": "health"`

        # Element 类型定义

        ## 1. dish_list（菜品列表）
        当识别到菜品数据时使用。
        ```json
        {
          "tag": "dish_list",
          "dishes": [
            {
              "dishId": "菜品ID",
              "dishName": "菜名",
              "imageUrl": "图片URL（可选）",
              "description": "描述（可选）",
              "price": 28.00,
              "rating": 4.8,
              "calories": 450,
              "category": "分类",
              "tags": ["标签1", "标签2"],
              "merchantName": "商家名",
              "merchantId": "商家ID"
            }
          ]
        }
        ```

        ## 2. order_list（订单列表）
        当识别到订单数据时使用。
        ```json
        {
          "tag": "order_list",
          "total": 5,
          "pendingCount": 2,
          "orders": [
            {
              "orderId": "订单号",
              "status": "delivering",
              "statusText": "配送中",
              "statusColor": "orange",
              "dishCount": 3,
              "totalAmount": 56.00,
              "createTime": "2026-04-03 12:30",
              "actions": []
            }
          ]
        }
        ```

        ## 3. health_stats（营养统计）
        当识别到营养/健康数据时使用。
        ```json
        {
          "tag": "health_stats",
          "stats": [
            { "label": "卡路里", "value": "1450/1800", "percent": 80, "color": "green" },
            { "label": "蛋白质", "value": "65g/80g", "percent": 81, "color": "blue" },
            { "label": "碳水", "value": "180g/250g", "percent": 72, "color": "orange" }
          ],
          "suggestion": "晚餐建议补充蛋白质"
        }
        ```

        ## 4. markdown（富文本内容）
        通用文本展示。
        ```json
        { "tag": "markdown", "content": "**加粗文本**\\n普通文本" }
        ```

        ## 5. note（提示/备注）
        ```json
        { "tag": "note", "content": "提示内容", "type": "info" }
        ```
        type可选值：info, warning, success, error

        ## 6. stats_row（统计数字行）
        ```json
        {
          "tag": "stats_row",
          "items": [
            { "label": "今日推荐", "value": 12, "color": "#ff6b00", "icon": "fire" },
            { "label": "好评率", "value": "98%", "color": "#52c41a", "icon": "star" }
          ]
        }
        ```

        ## 7. divider（分割线）
        ```json
        { "tag": "divider" }
        ```

        # Actions 定义

        ```json
        {
          "tag": "button",
          "text": "按钮文字",
          "type": "primary",
          "icon": "emoji（可选）",
          "disabled": false,
          "action": { "type": "action_type", "data": { "key": "value" } }
        }
        ```
        type可选值：primary, default, danger
        action.type示例：add_to_cart, view_detail, reorder, navigate

        # 转换示例

        ## 示例1：JSON代码块（菜品推荐）

        原始：
        "我为你推荐以下菜品：

        ```json
        {"items": [{"name": "宫保鸡丁", "price": 28, "merchant": "川味轩", "rating": 4.8, "calories": 450}]}
        ```

        这些菜品都很适合你的口味。"

        转换后：
        "我为你推荐以下菜品：

        [CARD_DATA_START]
        {
          "schema": "jaseat_card_v1",
          "header": {
            "title": { "text": "菜品推荐", "icon": "🍽️" },
            "subtitle": "为您找到1道菜品",
            "theme": "dish"
          },
          "elements": [
            {
              "tag": "dish_list",
              "dishes": [
                {
                  "dishId": "",
                  "dishName": "宫保鸡丁",
                  "merchantName": "川味轩",
                  "price": 28,
                  "rating": 4.8,
                  "calories": 450
                }
              ]
            }
          ],
          "footer": { "note": "以上菜品均符合您的口味偏好" },
          "displayMode": "inline"
        }
        [CARD_DATA_END]

        这些菜品都很适合你的口味。"

        ## 示例2：Markdown文本（菜品推荐）

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
        综合评分：82.30分"

        转换后：
        [CARD_DATA_START]
        {
          "schema": "jaseat_card_v1",
          "header": {
            "title": { "text": "菜品推荐", "icon": "🍽️" },
            "subtitle": "为您找到2道菜品",
            "theme": "dish"
          },
          "elements": [
            {
              "tag": "dish_list",
              "dishes": [
                {
                  "dishId": "dish123",
                  "dishName": "宫保鸡丁",
                  "merchantName": "川味轩",
                  "merchantId": "merchant123",
                  "price": 38,
                  "rating": 4.8,
                  "calories": 450
                },
                {
                  "dishId": "dish456",
                  "dishName": "皮蛋瘦肉粥",
                  "merchantName": "粤香阁",
                  "merchantId": "merchant456",
                  "price": 12,
                  "rating": 4.5,
                  "calories": 180
                }
              ]
            }
          ],
          "displayMode": "inline"
        }
        [CARD_DATA_END]

        ## 示例3：营养数据

        原始：
        "您今日的营养摄入情况：卡路里1450/1800千卡，蛋白质65g/80g，碳水180g/250g。建议晚餐补充蛋白质。"

        转换后：
        "您今日的营养摄入情况：

        [CARD_DATA_START]
        {
          "schema": "jaseat_card_v1",
          "header": {
            "title": { "text": "今日营养分析", "icon": "💪" },
            "subtitle": "2026-04-03",
            "theme": "health"
          },
          "elements": [
            {
              "tag": "health_stats",
              "stats": [
                { "label": "卡路里", "value": "1450/1800", "percent": 80, "color": "green" },
                { "label": "蛋白质", "value": "65g/80g", "percent": 81, "color": "blue" },
                { "label": "碳水", "value": "180g/250g", "percent": 72, "color": "orange" }
              ],
              "suggestion": "晚餐建议补充蛋白质"
            }
          ],
          "displayMode": "inline"
        }
        [CARD_DATA_END]

        建议晚餐补充蛋白质。"

        ## 示例4：不生成卡片的情况

        原始："很抱歉，没有找到符合条件的结果。"
        转换后："很抱歉，没有找到符合条件的结果。"（不生成卡片，直接返回原始文本）

        原始："请问您想要什么口味的菜品？"
        转换后："请问您想要什么口味的菜品？"（不生成卡片，直接返回原始文本）

        # 字段提取规则

        - dishId：从"菜品ID：xxx"提取
        - dishName：从"🍲 菜名"或"**菜名**"提取
        - price：从"¥xxx"提取数字
        - calories：从"xxx kcal"或"🔥 xxx"提取数字
        - rating/score：从"⭐ x.x分"提取数字
        - merchantName：从"商家：xxx"或"商家ID：xxx - xxx"提取
        - merchantId：从"商家ID：xxx"提取

        # 重要规则

        1. 卡片数据必须用 [CARD_DATA_START] 和 [CARD_DATA_END] 包围
        2. schema 字段固定为 "jaseat_card_v1"
        3. 只有包含实际结构化数据时才生成卡片，否则返回原始文本
        4. elements 数组至少包含1个元素
        5. actions 和 footer 是可选的
        6. displayMode 默认为 "inline"
        7. 只返回格式化后的结果，不要添加额外的解释
        """)
    @Agent("卡片格式化专家，负责将AI回复转换为统一的UniCard Schema卡片格式")
    String renderCards(@V("originalResult") String originalResult);
}
