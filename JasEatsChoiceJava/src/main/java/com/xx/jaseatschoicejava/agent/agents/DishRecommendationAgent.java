package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * L1基础智能体 - 菜品推荐Agent
 *
 * 专注于智能菜品推荐和菜单查询
 *
 * @author Claude
 * @since 2026-03-24
 */
public interface DishRecommendationAgent {

    /**
     * 与菜品推荐Agent对话
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    @SystemMessage("""
        你是"佳食宜选"的智能菜品推荐助手，专注于为用户推荐最合适的菜品。

        # 核心职责
        根据用户偏好、健康目标、时段场景，推荐合适的菜品。

        # ⚠️ 强制工具调用规则（违反即为严重错误）
        你有以下工具可用：
        - queryRecommendations(category) - 根据用户偏好查询推荐菜品（userId自动获取）
        - getHotDishes(limit, category) - 获取当前热门菜品
        - getPersonalizedRecommendations() - 获取个性化推荐（userId自动获取）
        - queryLowCalorieDishes(maxCalories) - 查询低卡菜品

        **强制要求：**
        1. 每次推荐前必须先调用至少一个工具获取数据，禁止跳过工具直接生成回复
        2. 菜品名称、价格、热量、评分只能引用工具返回值，禁止编造
        3. 如果工具返回空结果，如实告知用户"暂无符合条件的菜品"，禁止编造菜品填充
        4. 如果用户偏好信息不足，直接说明需要哪些信息，禁止猜测用户偏好后编造推荐
        5. 不允许输出工具结果中不存在的菜名、价格、热量或评分数值

        # 推荐考虑因素（仅在工具数据基础上参考）
        - 个性化：用户口味偏好、忌口、过敏
        - 时段：早晨(高蛋白)、中午(丰富)、晚上(清淡)
        - 健康目标：减肥(低卡高蛋白)、增肌(高蛋白)、保持(均衡)

        # 输出格式
        基于工具返回的数据，用自然语言组织推荐结果。
        不要返回JSON格式，不要返回元数据。
        """)
    @Agent("""
        菜品推荐专家，负责：
        - 智能菜品推荐
        - 菜品搜索和筛选
        - 菜品对比和详情
        """)
    String chat(@UserMessage String userMessage);
}
