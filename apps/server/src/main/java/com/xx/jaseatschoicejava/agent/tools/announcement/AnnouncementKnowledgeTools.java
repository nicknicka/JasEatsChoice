package com.xx.jaseatschoicejava.agent.tools.announcement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.xx.jaseatschoicejava.agent.context.AgentChatContext;
import com.xx.jaseatschoicejava.rag.RagAudience;
import com.xx.jaseatschoicejava.rag.RagKnowledgeQuery;
import com.xx.jaseatschoicejava.rag.RagKnowledgeResult;
import com.xx.jaseatschoicejava.rag.RagKnowledgeService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.invocation.LangChain4jManaged;

/**
 * 公告知识检索工具。
 *
 * 权限只来自 AgentChatContextListener 写入的 AgenticScope；
 * 商家和用户范围由可信聊天上下文决定。
 */
@Service
public class AnnouncementKnowledgeTools {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementKnowledgeTools.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RagKnowledgeService knowledgeService;

    public AnnouncementKnowledgeTools(RagKnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Tool("""
        检索当前有效且当前角色可见的平台公告和商家公告。

        优先用于平台规则、活动、公告、满减、优惠和退款说明等知识问题。
        商家和用户范围由可信聊天上下文决定，不接受工具参数指定。
        可选 limit 控制返回条数；未填写时默认 3。

        返回公告标题、原文片段和有效期；没有命中时明确说明未查询到。
        """)
    public String searchAnnouncements(
        @P("检索问题或关键词") String query,
        @P(value = "返回数量，可选", required = false) Integer limit
    ) {
        AgenticScope scope = currentScope();
        if (scope == null) {
            return "公告检索暂不可用，请重新登录后再试。";
        }

        try {
            String audienceValue = (String) scope.readState("audience");
            RagAudience audience = audience(scope, audienceValue);
            String merchantId = null;
            if (AgentChatContext.AUDIENCE_MERCHANT.equals(audienceValue)) {
                merchantId = (String) scope.readState("merchantId");
                if (merchantId == null || merchantId.isBlank()) {
                    return "未查询到相关有效公告。";
                }
            }

            List<RagKnowledgeResult> results = knowledgeService.search(new RagKnowledgeQuery(
                query,
                audience,
                merchantId,
                LocalDateTime.now(),
                limit == null ? 3 : limit
            ));
            return format(results);
        }
        catch (Exception exception) {
            log.warn("公告知识检索失败", exception);
            return "公告检索暂不可用，请稍后再试。";
        }
    }

    /**
     * 读取当前 Agentic 托管上下文里的 scope。
     *
     * 不能把 AgenticScope 声明成工具参数：那类托管参数只在 Agentic 调用链内可用，
     * 客服 Agent 被直连调用时 prepareArguments 会因 managedParameters() 为 null 抛 NPE。
     * 这里主动读取同一份托管上下文，拿不到时按“失败关闭”处理。
     */
    private AgenticScope currentScope() {
        Map<Class<? extends LangChain4jManaged>, LangChain4jManaged> managed =
            LangChain4jManaged.current();
        return managed == null ? null : (AgenticScope) managed.get(AgenticScope.class);
    }

    private RagAudience audience(AgenticScope scope, String audienceValue) {
        if (AgentChatContext.AUDIENCE_MERCHANT.equals(audienceValue)) {
            return RagAudience.MERCHANT;
        }
        if (AgentChatContext.AUDIENCE_CUSTOMER.equals(audienceValue)) {
            return RagAudience.CUSTOMER;
        }
        throw new IllegalArgumentException("audience is required");
    }

    private String format(List<RagKnowledgeResult> results) {
        if (results == null || results.isEmpty()) {
            return "未查询到相关有效公告。";
        }

        StringBuilder answer = new StringBuilder("根据当前有效公告：\n");
        for (int index = 0; index < results.size(); index++) {
            RagKnowledgeResult result = results.get(index);
            answer.append(index + 1).append(". 《").append(result.title()).append("》：")
                .append(result.text()).append('\n')
                .append("   有效期：").append(effectiveRange(result)).append('\n');
        }
        return answer.toString().strip();
    }

    private String effectiveRange(RagKnowledgeResult result) {
        if (result.startTime() == null && result.endTime() == null) {
            return "未设置";
        }
        if (result.startTime() == null) {
            return "截至 " + TIME_FORMAT.format(result.endTime());
        }
        if (result.endTime() == null) {
            return "自 " + TIME_FORMAT.format(result.startTime()) + " 起";
        }
        return TIME_FORMAT.format(result.startTime()) + " 至 " + TIME_FORMAT.format(result.endTime());
    }

}
