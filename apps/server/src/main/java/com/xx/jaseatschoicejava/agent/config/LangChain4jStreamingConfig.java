package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.agents.stream.StreamingResponseAgent;
import com.xx.jaseatschoicejava.config.AIConfig;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

/**
 * LangChain4j流式输出配置类
 *
 * 仅配置 StreamingResponseAgent，用于 Supervisor 架构的流式输出阶段。
 * Supervisor 同步收集数据 → StreamingResponseAgent 流式输出结果。
 *
 * 模型通过 ChatModelProvider 获取，与具体供应商解耦。
 *
 * @since 2026-03-24
 * @updated 2026-04-03 清理废弃Agent，仅保留StreamingResponseAgent
 * @updated 2026-10-27 供应商解耦：通过 ChatModelProvider 获取流式模型
 */
@Configuration
@EnableConfigurationProperties(AIConfig.class)
public class LangChain4jStreamingConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jStreamingConfig.class);

    @Resource
    private AIConfig aiConfig;

    @Resource(name = "chatModelProvider")
    private ChatModelProvider chatModelProvider;

    private StreamingChatModel streamingChatLanguageModel;

    /**
     * 配置 StreamingChatModel（通过 ChatModelProvider 获取，与供应商无关）
     */
    @Bean(destroyMethod = "")
    public StreamingChatModel streamingChatLanguageModel() {
        if (chatModelProvider == null || !chatModelProvider.isAvailable()) {
            log.warn("AI API Key 未配置，StreamingChatModel 降级为占位实现：流式接口会直接回调错误");
            this.streamingChatLanguageModel = new NoopStreamingChatModel();
            return this.streamingChatLanguageModel;
        }

        log.info("初始化StreamingChatModel（供应商：{}），模型：{}", aiConfig.getProvider(), aiConfig.getModel());

        this.streamingChatLanguageModel = chatModelProvider.buildStreamingModel();
        return this.streamingChatLanguageModel;
    }

    /**
     * 构建流式响应 Agent
     *
     * Supervisor 架构的流式输出阶段：
     * - 不挂载工具类，纯 LLM 生成
     * - 接收 Supervisor 同步结果，逐字流式输出 + 卡片渲染
     */
    @Bean
    public StreamingResponseAgent streamingResponseAgent(
            StreamingChatModel streamingChatLanguageModel) {
        log.info("构建 StreamingResponseAgent（流式响应 + 卡片渲染）...");

        return AiServices.builder(StreamingResponseAgent.class)
                .streamingChatModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    @PreDestroy
    public void cleanup() {
        log.info("LangChain4j流式资源清理完成");
    }
}
