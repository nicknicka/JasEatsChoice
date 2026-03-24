package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * 流式Agent服务
 * 处理流式输出的AI对话
 *
 * @author Claude
 * @since 2026-03-24
 */
@Service
public class StreamingAgentService {

    private static final Logger log = LoggerFactory.getLogger(StreamingAgentService.class);

    @Resource
    private StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent;

    /**
     * 流式对话 - 使用回调处理每个token
     *
     * @param userMessage 用户消息
     * @param tokenHandler token处理器，接收每个生成的token
     */
    public void chatStream(String userMessage, TokenHandler tokenHandler) {
        log.info("收到流式对话请求: {}", userMessage);

        TokenStream tokenStream = streamingIntelligentAssistantAgent.chat(userMessage);

        tokenStream.onNext(token -> {
            // 处理每个token（token是String类型）
            if (token != null && !token.isEmpty()) {
                try {
                    tokenHandler.onToken(token);
                } catch (Exception e) {
                    log.error("处理token时出错", e);
                }
            }
        })
        .onComplete(response -> {
            // 流结束时调用（response包含完整的AiMessage）
            log.info("流式对话完成");
            try {
                tokenHandler.onComplete();
            } catch (Exception e) {
                log.error("处理完成回调时出错", e);
            }
        })
        .onError(error -> {
            // 处理错误
            log.error("流式对话出错", error);
            try {
                tokenHandler.onError(error);
            } catch (Exception e) {
                log.error("处理错误回调时出错", e);
            }
        })
        .start(); // 启动流式处理
    }

    /**
     * Token处理器接口
     */
    public interface TokenHandler {
        /**
         * 处理每个token
         *
         * @param token 文本片段
         */
        void onToken(String token) throws IOException;

        /**
         * 流完成时调用
         */
        void onComplete() throws IOException;

        /**
         * 发生错误时调用
         *
         * @param error 错误信息
         */
        void onError(Throwable error) throws IOException;
    }
}
