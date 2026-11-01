package com.xx.jaseatschoicejava.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import com.xx.jaseatschoicejava.agent.tools.announcement.AnnouncementKnowledgeTools;
import com.xx.jaseatschoicejava.rag.RagKnowledgeService;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * 知识客服 Agent 调用公告检索工具的回归用例。
 *
 * 生产上匿名客服走的是直连调用（LangChain4jConfig 里未挂 Agentic 监听器），
 * 此时不存在 Agentic 托管上下文。工具若把 AgenticScope 声明成参数，
 * DefaultToolExecutor.prepareArguments 会因 managedParameters() 为 null 抛 NPE，
 * 让整轮对话直接失败——必须退化为“失败关闭”并照常给出回复。
 *
 * 带可信身份的检索过滤行为见 {@link com.xx.jaseatschoicejava.agent.tools.announcement.AnnouncementKnowledgeToolsTest}。
 */
class AnnouncementToolInvocationTest {

    @Test
    void customerServiceAgentWithoutTrustedContextFailsClosed() {
        AtomicInteger modelCalls = new AtomicInteger();
        RagKnowledgeService knowledgeService = mock(RagKnowledgeService.class);

        CustomerServiceAgent agent = AgenticServices.agentBuilder(CustomerServiceAgent.class)
            .chatModel(scriptedModel(modelCalls))
            .tools(new AnnouncementKnowledgeTools(knowledgeService))
            .build();

        String answer = agent.chat("平台最近有什么公告？");

        assertEquals("已根据公告回答", answer);
        assertEquals(2, modelCalls.get());
        verifyNoInteractions(knowledgeService);
    }

    private ChatModel scriptedModel(AtomicInteger modelCalls) {
        return new ChatModel() {
            @Override
            public ChatResponse chat(ChatRequest request) {
                if (modelCalls.getAndIncrement() == 0) {
                    return ChatResponse.builder()
                        .aiMessage(AiMessage.builder()
                            .toolExecutionRequests(List.of(ToolExecutionRequest.builder()
                                .id("call-1")
                                .name("searchAnnouncements")
                                .arguments("{\"query\":\"公告\",\"limit\":1}")
                                .build()))
                            .build())
                        .build();
                }
                return ChatResponse.builder().aiMessage(AiMessage.from("已根据公告回答")).build();
            }
        };
    }
}
