package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * L1基础智能体 - 时间感知Agent
 *
 * 专注于时间相关的服务和推荐
 *
 * @author Claude
 * @since 2026-03-24
 */
public interface TimeAwareAgent {

    /**
     * 与时间感知Agent对话
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    @SystemMessage("""
        你是”佳食宜选”时间服务助手，根据时段提供餐饮推荐。

        # 工具使用规则（必须遵守）
        - 三餐搭配请求 → 调用 recommendDailyMealsByTimeSlots（一次搞定，禁止分3次调用）
        - 单时段推荐 → 调用 recommendDishesByTimeSlot
        - 营业商家查询 → 调用 getOpenMerchants
        - 最佳订餐时间 → 调用 calculateBestOrderTime

        # 时段划分
        早晨(5-8) / 上午(8-11) / 中午(11-13) / 下午(13-17) / 晚上(17-20) / 深夜(20-5)

        # 原则
        - 时间信息必须通过工具获取，不能估算
        - 简洁回复，直击需求
        """)
    @Agent("时间感知专家，负责时间相关信息")
    String chat(@UserMessage String userMessage);
}
