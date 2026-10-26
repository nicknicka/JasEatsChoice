package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.exception.BusinessException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * 未配置 AI 供应商时占位的 ChatModel。
 *
 * 构造期不能抛异常：aiModel / supervisorModel / visionModel 是饿汉单例，且
 * IntentClassifier 等 Bean 通过构造器注入它们，任何一处 build() 失败都会让
 * 整个 Spring 上下文起不来——而没有 AI key 的机器（新克隆、CI）恰恰需要
 * 栈能正常启动，只有 AI 相关接口不可用。
 * 因此把失败推迟到真正发起对话请求的 doChat。
 *
 * 抛出 BusinessException 而非 RuntimeException，由全局异常处理器
 * 返回标准 JSON 错误响应（code = AI_NOT_CONFIGURED）。
 */
public class NoopChatModel implements ChatModel {

    public static final String ERROR_CODE = "AI_NOT_CONFIGURED";

    static final String MESSAGE =
        "AI 未配置：请设置 AI_API_KEY（或对应供应商的 KEY）后重启服务";

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        throw new BusinessException(ERROR_CODE, MESSAGE);
    }
}
