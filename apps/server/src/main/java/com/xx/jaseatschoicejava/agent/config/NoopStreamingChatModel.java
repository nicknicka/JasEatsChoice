package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.exception.BusinessException;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * 未配置 AI 供应商时占位的流式 ChatModel。
 *
 * 同 {@link NoopChatModel}：构造期不能抛异常，否则 Spring 上下文起不来。
 * 流式接口是回调式的，所以失败通过 handler.onError 交回调用方，而不是把异常
 * 抛回调用线程。
 */
public class NoopStreamingChatModel implements StreamingChatModel {

    @Override
    public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
        handler.onError(new BusinessException(NoopChatModel.ERROR_CODE, NoopChatModel.MESSAGE));
    }
}
