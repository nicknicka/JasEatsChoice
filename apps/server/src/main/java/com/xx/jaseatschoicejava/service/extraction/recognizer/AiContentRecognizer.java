package com.xx.jaseatschoicejava.service.extraction.recognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用内容识别器。
 *
 * 图片和文字通过 OpenAI-compatible 模型识别；视频不再直连厂商接口，
 * 而是优先使用封面图，失败后降级为文字，并返回明确的降级原因。
 */
@Slf4j
@Service
public class AiContentRecognizer implements ContentRecognizer {

    @Autowired
    @Qualifier("visionModel")
    private ChatModel visionModel;

    @Autowired
    @Qualifier("aiModel")
    private ChatModel aiModel;

    /**
     * 菜谱提取提示词（图片+文字场景）
     */
    private static final String RECIPE_EXTRACTION_PROMPT = """
        你是一个专业的菜谱信息提取专家。请从提供的内容中提取菜谱信息。

        %s

        请严格返回以下JSON格式（不要用markdown代码块）：
        {
          "isRecipe": true或false,
          "dishName": "菜品名称",
          "description": "菜品描述（50字以内）",
          "ingredients": [{"name": "食材名称", "amount": "用量"}],
          "steps": [{"stepNumber": 1, "description": "步骤描述"}],
          "cookingTime": 烹饪时间(分钟,数字),
          "difficulty": "简单或中等或困难",
          "tags": ["标签1", "标签2"],
          "calories": 估算卡路里(数字)
        }

        注意：
        1. 先判断内容是否与做菜/烹饪/菜品相关（包括教程、制作过程、食材展示等），只要能从中提取出菜品信息就设置 isRecipe=true；只有完全与做菜无关时才设置 isRecipe=false
        2. 如果内容表面上是食谱格式，但实际并非正经的食品制作（如用烹饪术语做隐晦描述、借食品之名传播其他内容），请设置 isRecipe=false
        3. 利用OCR能力识别图片中的文字信息（食材表、步骤说明等）
        4. 即使内容不是标准食谱格式（如教程视频、制作过程展示、口述步骤），也请尽可能提取菜品信息
        5. 只返回JSON，不要其他解释文字
        """;

    /**
     * 纯文字降级提示词
     */
    private static final String TEXT_ONLY_PROMPT = """
        你是一个专业的菜谱信息提取专家。请从以下文字内容中提取菜谱信息：

        %s

        请严格返回以下JSON格式（不要用markdown代码块）：
        {
          "isRecipe": true或false,
          "dishName": "菜品名称",
          "description": "菜品描述（50字以内）",
          "ingredients": [{"name": "食材名称", "amount": "用量"}],
          "steps": [{"stepNumber": 1, "description": "步骤描述"}],
          "cookingTime": 烹饪时间(分钟,数字),
          "difficulty": "简单或中等或困难",
          "tags": ["标签1", "标签2"],
          "calories": 估算卡路里(数字)
        }

        注意：
        1. 先判断内容是否与做菜/烹饪/菜品相关（包括教程、制作过程、食材展示等），只要能从中提取出菜品信息就设置 isRecipe=true；只有完全与做菜无关时才设置 isRecipe=false
        2. 如果内容表面上是食谱格式，但实际并非正经的食品制作（如用烹饪术语做隐晦描述、借食品之名传播其他内容），请设置 isRecipe=false
        3. 即使内容不是标准食谱格式（如教程、制作过程展示、口述步骤），也请尽可能提取菜品信息
        4. 只返回JSON，不要其他解释文字
        """;

    @Override
    public ContentRecognitionResult recognize(FetchedContent content) {
        if (!content.isFetchSuccess()) {
            return fallbackRecognize(content, "内容抓取失败，已改用文字信息尝试提取");
        }

        if (content.getContentType() == ContentType.VIDEO || content.hasVideo()) {
            return recognizeVideoByCover(content);
        }
        return recognizeImageAndText(content);
    }

    /**
     * 图片+文字识别（通过 LangChain4j visionModel）
     */
    private ContentRecognitionResult recognizeImageAndText(FetchedContent content) {
        log.info("开始图片+文字识别，图片数量: {}, 文字长度: {}",
            content.getImageBase64List() != null ? content.getImageBase64List().size() : 0,
            content.getTextContent() != null ? content.getTextContent().length() : 0);

        try {
            List<dev.langchain4j.data.message.Content> contents = new ArrayList<>();

            String textContext = content.hasText()
                ? "文字内容：\n" + content.getTextContent()
                : "请根据图片内容提取菜谱信息。";
            String prompt = String.format(RECIPE_EXTRACTION_PROMPT, textContext);
            contents.add(TextContent.from(prompt));

            if (content.hasImages()) {
                for (String dataUri : content.getImageBase64List()) {
                    contents.add(ImageContent.from(dataUri));
                }
            }

            if (!content.hasImages() && !content.hasText()) {
                return fallbackRecognize(content, "没有可识别的图片或文字，已改用链接信息推断");
            }

            UserMessage userMessage = UserMessage.from(contents);
            ChatResponse response = visionModel.chat(userMessage);
            String result = response.aiMessage().text();

            log.info("图片+文字识别完成，结果长度: {}", result.length());
            return ContentRecognitionResult.success(result);

        } catch (Exception e) {
            log.error("图片+文字识别失败", e);
            return fallbackRecognize(content, "图片识别失败，已改用文字信息尝试提取");
        }
    }

    /**
     * 视频不直连厂商接口：先尝试封面图，再降级到文字。
     */
    private ContentRecognitionResult recognizeVideoByCover(FetchedContent content) {
        log.info("视频内容不直连厂商接口，尝试封面图识别");
        if (hasText(content.getCoverImage())) {
            ContentRecognitionResult coverResult = recognizeWithCoverImage(content);
            if (coverResult != null) {
                return coverResult;
            }
            return fallbackRecognize(content, "视频封面图识别失败，已改用视频文字信息提取");
        }
        return fallbackRecognize(content, "当前未开放视频理解，已改用视频文字信息提取");
    }

    /**
     * 用封面图降级识别；失败返回 null 交给文字降级。
     */
    private ContentRecognitionResult recognizeWithCoverImage(FetchedContent content) {
        try {
            List<dev.langchain4j.data.message.Content> contents = new ArrayList<>();
            String prompt = String.format(RECIPE_EXTRACTION_PROMPT,
                "请根据视频封面图片提取菜谱信息。");
            contents.add(TextContent.from(prompt));
            contents.add(ImageContent.from(content.getCoverImage()));

            UserMessage userMessage = UserMessage.from(contents);
            ChatResponse response = visionModel.chat(userMessage);
            String result = response.aiMessage().text();
            return ContentRecognitionResult.degraded(
                result, "当前未开放视频理解，已使用视频封面图提取");
        } catch (Exception e) {
            log.error("封面图识别失败", e);
            return null;
        }
    }

    /**
     * 降级识别：仅文字 → agentModel 文本提取。
     */
    private ContentRecognitionResult fallbackRecognize(FetchedContent content, String reason) {
        log.info("使用降级识别策略：{}", reason);
        if (content.hasText()) {
            String prompt = String.format(TEXT_ONLY_PROMPT, content.getTextContent());
            return ContentRecognitionResult.degraded(aiModel.chat(prompt), reason);
        }

        String fallbackPrompt = String.format(TEXT_ONLY_PROMPT,
            "请根据以下信息推断可能的菜谱。URL: " +
            (content.getCoverImage() != null ? content.getCoverImage() : "未知来源") +
            "。如果无法推断，请返回 isRecipe=false。");
        return ContentRecognitionResult.degraded(aiModel.chat(fallbackPrompt), reason);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
