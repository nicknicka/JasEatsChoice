package com.xx.jaseatschoicejava.agent.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * 未配置 ZHIPU_API_KEY 时占位的 ChatModel。
 *
 * 构造期不能抛异常：aiModel / supervisorModel / visionModel 是饿汉单例，且
 * IntentClassifier 等 Bean 通过构造器注入它们，任何一处 build() 失败都会让
 * 整个 Spring 上下文起不来——而没有 AI key 的机器（新克隆、CI）恰恰需要
 * 栈能正常启动，只有 AI 相关接口不可用。
 * 因此把失败推迟到真正发起对话请求的 doChat。
 */
public class ZhipuDisabledChatModel implements ChatModel {

    static final String MESSAGE =
        "智谱 AI 未配置：请在 deploy/.env 设置 ZHIPU_API_KEY 后重启 spring-boot 服务";

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        throw new IllegalStateException(MESSAGE);
    }
}
