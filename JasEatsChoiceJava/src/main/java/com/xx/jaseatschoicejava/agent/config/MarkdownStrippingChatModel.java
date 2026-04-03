package com.xx.jaseatschoicejava.agent.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * ChatModel装饰器，自动剥离LLM响应中的Markdown代码块标记
 *
 * 解决GLM模型返回 ```json ... ``` 导致langchain4j JSON解析失败的问题。
 * 某些LLM（如GLM-4）倾向于将JSON输出包裹在markdown代码块中，
 * 而langchain4j的JsonParsingUtils.extractAndParseJson无法正确处理这种格式。
 *
 * 装饰器在响应返回后自动检测并剥离以下格式：
 * - ```json\n{...}\n```
 * - ```JSON\n{...}\n```
 * - ```\n{...}\n```
 *
 * @author Claude
 * @since 2026-04-03
 */
public class MarkdownStrippingChatModel implements ChatModel {

    private static final Logger log = LoggerFactory.getLogger(MarkdownStrippingChatModel.class);

    /**
     * 匹配markdown代码块标记的正则表达式
     * 支持多种格式：```json, ```JSON, ``` 等
     */
    private static final Pattern MARKDOWN_CODE_BLOCK_PATTERN = Pattern.compile(
        "^\\s*```(?:json|JSON)?\\s*\\n?(.*?)\\n?\\s*```\\s*$",
        Pattern.DOTALL
    );

    private final ChatModel delegate;

    public MarkdownStrippingChatModel(ChatModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatResponse response = delegate.chat(request);

        AiMessage original = response.aiMessage();
        String text = original.text();

        if (text != null && containsMarkdownCodeBlock(text)) {
            String stripped = stripMarkdownCodeBlock(text);
            if (!text.equals(stripped)) {
                log.debug("已剥离Markdown代码块标记，原始长度: {}，清理后长度: {}",
                    text.length(), stripped.length());

                AiMessage newMessage = AiMessage.from(stripped);
                return ChatResponse.builder()
                    .aiMessage(newMessage)
                    .metadata(response.metadata())
                    .build();
            }
        }

        return response;
    }

    /**
     * 检测文本是否包含markdown代码块标记
     */
    private boolean containsMarkdownCodeBlock(String text) {
        String trimmed = text.trim();
        return trimmed.startsWith("```");
    }

    /**
     * 剥离Markdown代码块标记
     * 使用正则表达式处理，支持多种格式
     */
    private String stripMarkdownCodeBlock(String text) {
        String trimmed = text.trim();

        // 使用正则匹配并提取内容
        var matcher = MARKDOWN_CODE_BLOCK_PATTERN.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }

        // 正则未匹配时，使用简单的字符串剥离作为降级方案
        String result = trimmed;

        // 剥离开头的 ```json 或 ```
        if (result.startsWith("```json")) {
            result = result.substring(7);
        } else if (result.startsWith("```JSON")) {
            result = result.substring(7);
        } else if (result.startsWith("```")) {
            result = result.substring(3);
        }

        // 剥离结尾的 ```
        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - 3);
        }

        return result.trim();
    }
}
