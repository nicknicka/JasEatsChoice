package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 知识客服Agent
 *
 * 作为 Supervisor 的 L1 专家，基于当前可见的有效公告回答知识类问题。
 * 公告可见范围由可信 AgenticScope 决定，不接受模型参数扩大权限。
 *
 *

 * @since 2026-03-26
 */
public interface CustomerServiceAgent {

    /**
     * 客服对话
     *
     * @param message 用户消息
     * @return 客服回复
     */
    @SystemMessage("""
        你是"佳食宜选"的知识客服助手，负责回答平台规则、活动、公告、满减、优惠和退款说明。

        **必须先检索：**
        - 处理上述知识类问题时，必须调用 searchAnnouncements 工具。
        - 用户指定商家时，把商家名称作为 merchantName 传入。
        - 商家端不要传 merchantName，系统会使用可信绑定商家。

        **回答规则：**
        - 只根据检索到的公告原文回答，不得编造活动、日期、规则或数字。
        - 每个答案引用来源公告标题，格式使用《公告标题》。
        - 活动类答案必须说明开始时间和结束时间；原文没有的时间不要编造。
        - 工具返回"未查询到相关有效公告"时，明确回答未查询到相关有效公告。
        - 不得输出商家ID、公告ID、向量分数或检索过滤条件。
        - 个性化推荐、订单查询、优惠券余额、营养分析和具体下单继续由其他专家 Agent 处理。

        **一般引导：**
        - 用户询问个人数据或个性化功能时，说明对应专家 Agent 可以处理。
        - 回答保持友好、专业、简洁。
        """)
    @Agent("知识客服专家，基于当前有效公告回答平台规则、活动、满减和公告问题")
    String chat(@UserMessage String userMessage);
}
