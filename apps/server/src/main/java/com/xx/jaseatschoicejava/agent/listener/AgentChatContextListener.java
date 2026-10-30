package com.xx.jaseatschoicejava.agent.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xx.jaseatschoicejava.agent.context.AgentChatContext;

import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.scope.AgenticScope;

/**
 * 将可信聊天上下文写入 AgenticScope，供工具统一读取。
 */
public class AgentChatContextListener implements AgentListener {

    private static final Logger log = LoggerFactory.getLogger(AgentChatContextListener.class);

    private final AgentChatContext context;

    public AgentChatContextListener(AgentChatContext context) {
        this.context = context;
    }

    @Override
    public void afterAgenticScopeCreated(AgenticScope scope) {
        if (context == null) {
            return;
        }

        scope.writeState("userId", context.getUserId());
        scope.writeState("audience", context.getAudience());
        if (context.isMerchant()) {
            scope.writeState("merchantId", context.getMerchantId());
        }
        log.info("🔑 [AgenticScope] 已写入可信上下文: userId={}, audience={}",
                context.getUserId(), context.getAudience());
    }
}
