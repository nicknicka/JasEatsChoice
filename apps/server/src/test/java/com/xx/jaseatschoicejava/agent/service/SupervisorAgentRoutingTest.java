package com.xx.jaseatschoicejava.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.DishRecommendationAgent;
import com.xx.jaseatschoicejava.agent.agents.LocationServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.MerchantInfoAgent;
import com.xx.jaseatschoicejava.agent.agents.NutritionGuideAgent;
import com.xx.jaseatschoicejava.agent.agents.OrderHelperAgent;
import com.xx.jaseatschoicejava.agent.agents.TimeAwareAgent;
import com.xx.jaseatschoicejava.agent.agents.UserPreferenceAgent;
import com.xx.jaseatschoicejava.agent.config.ChatMemoryFactory;
import com.xx.jaseatschoicejava.agent.tools.announcement.AnnouncementKnowledgeTools;
import com.xx.jaseatschoicejava.agent.context.AgentChatContext;
import com.xx.jaseatschoicejava.rag.RagAudience;
import com.xx.jaseatschoicejava.rag.RagKnowledgeQuery;
import com.xx.jaseatschoicejava.rag.RagKnowledgeResult;
import com.xx.jaseatschoicejava.rag.RagKnowledgeService;
import com.xx.jaseatschoicejava.agent.agents.CardRendererAgent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

class SupervisorAgentRoutingTest {

    private final ChatModel supervisorModel = new FixedRouteChatModel();
    private final ChatMemoryFactory chatMemoryFactory = mock(ChatMemoryFactory.class);
    private final RagKnowledgeService knowledgeService = mock(RagKnowledgeService.class);
    private final CardRendererAgent cardRendererAgent = agent(CardRendererAgent.class, "CardRendererAgent", "已渲染");
    private final CustomerServiceAgent customerServiceAgent = AgenticServices.agentBuilder(CustomerServiceAgent.class)
        .chatModel(new ToolThenAnswerChatModel())
        .name("CustomerServiceAgent")
        .description("知识客服专家，基于公告回答平台规则和活动问题")
        .tools(new AnnouncementKnowledgeTools(knowledgeService))
        .build();
    private final DishRecommendationAgent dishRecommendationAgent = agent(DishRecommendationAgent.class, "DishRecommendationAgent", "未使用");
    private final UserPreferenceAgent userPreferenceAgent = agent(UserPreferenceAgent.class, "UserPreferenceAgent", "未使用");
    private final NutritionGuideAgent nutritionGuideAgent = agent(NutritionGuideAgent.class, "NutritionGuideAgent", "未使用");
    private final OrderHelperAgent orderHelperAgent = agent(OrderHelperAgent.class, "OrderHelperAgent", "订单回答");
    private final MerchantInfoAgent merchantInfoAgent = agent(MerchantInfoAgent.class, "MerchantInfoAgent", "未使用");
    private final TimeAwareAgent timeAwareAgent = agent(TimeAwareAgent.class, "TimeAwareAgent", "未使用");
    private final LocationServiceAgent locationServiceAgent = agent(LocationServiceAgent.class, "LocationServiceAgent", "未使用");
    private final SupervisorAgentFactory factory = new SupervisorAgentFactory(
        supervisorModel,
        chatMemoryFactory,
        cardRendererAgent,
        customerServiceAgent,
        dishRecommendationAgent,
        userPreferenceAgent,
        nutritionGuideAgent,
        orderHelperAgent,
        merchantInfoAgent,
        timeAwareAgent,
        locationServiceAgent
    );

    @Test
    void routesKnowledgeQuestionsToCustomerServiceAgent() {
        when(chatMemoryFactory.createChatMemory(anyString()))
            .thenReturn(MessageWindowChatMemory.withMaxMessages(10));
        when(knowledgeService.search(any())).thenReturn(List.of(knowledgeResult()));

        String answer = factory.createWithListener(null, AgentChatContext.customer("1001"))
            .invoke("现在有什么平台活动？");

        assertThat(answer).contains("《周末满减》").contains("满30减5");
        ArgumentCaptor<RagKnowledgeQuery> captor = ArgumentCaptor.forClass(RagKnowledgeQuery.class);
        verify(knowledgeService).search(captor.capture());
        assertThat(captor.getValue().audience()).isEqualTo(RagAudience.CUSTOMER);
        assertThat(captor.getValue().merchantId()).isNull();
    }

    @Test
    void routesOrderQuestionsToOrderHelperAgent() {
        when(chatMemoryFactory.createChatMemory(anyString()))
            .thenReturn(MessageWindowChatMemory.withMaxMessages(10));

        String answer = factory.createWithListener(null, AgentChatContext.customer("1001"))
            .invoke("查一下我的订单");

        assertThat(answer).isEqualTo("订单回答");
    }

    private static <T> T agent(Class<T> type, String name, String answer) {
        return AgenticServices.agentBuilder(type)
            .chatModel(new FixedAnswerChatModel(answer))
            .name(name)
            .build();
    }

    private RagKnowledgeResult knowledgeResult() {
        return new RagKnowledgeResult(
            "announcement-1",
            "周末满减",
            "满30减5，周末可用。",
            0,
            1,
            null,
            "customer",
            null,
            null);
    }

    private record FixedAnswerChatModel(String answer) implements ChatModel {
        @Override
        public ChatResponse doChat(ChatRequest request) {
            return ChatResponse.builder().aiMessage(AiMessage.from(answer())).build();
        }
    }

    private static class ToolThenAnswerChatModel implements ChatModel {
        private int calls;

        @Override
        public ChatResponse doChat(ChatRequest request) {
            if (calls++ == 0) {
                ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .id("tool-call-1")
                    .name("searchAnnouncements")
                    .arguments("{\"query\":\"平台活动\"}")
                    .build();
                return ChatResponse.builder().aiMessage(AiMessage.from(toolRequest)).build();
            }
            ToolExecutionResultMessage toolResult = request.messages().stream()
                .filter(ToolExecutionResultMessage.class::isInstance)
                .map(ToolExecutionResultMessage.class::cast)
                .findFirst()
                .orElseThrow();
            return ChatResponse.builder().aiMessage(AiMessage.from(toolResult.text())).build();
        }
    }

    private static class FixedRouteChatModel implements ChatModel {
        private boolean routed;

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().getLast().toString();
            if (prompt.contains("Create a short summary")) {
                return ChatResponse.builder()
                    .aiMessage(AiMessage.from("{\"summary\":\"已完成\"}"))
                    .build();
            }
            if (!routed) {
                routed = true;
                String agentName = prompt.contains("平台活动")
                    ? "CustomerServiceAgent"
                    : "OrderHelperAgent";
                String task = prompt.contains("平台活动")
                    ? "查询平台活动公告"
                    : "查询用户订单";
                return ChatResponse.builder()
                    .aiMessage(AiMessage.from(
                        "{\"agentName\":\"" + agentName + "\",\"arguments\":{\"userMessage\":\"" + task + "\"}}"))
                    .build();
            }
            return ChatResponse.builder()
                .aiMessage(AiMessage.from("{\"agentName\":\"done\",\"arguments\":{\"summary\":\"已完成\"}}"))
                .build();
        }
    }
}
