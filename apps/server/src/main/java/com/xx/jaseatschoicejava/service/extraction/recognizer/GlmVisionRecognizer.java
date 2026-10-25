package com.xx.jaseatschoicejava.service.extraction.recognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * GLM-4.6V-Flash 内容识别器
 * 负责将抓取到的内容交给AI进行识别和结构化
 *
 * 图片+文字：通过 LangChain4j visionModel
 * 视频：直接通过 HTTP 调用智谱 API（LangChain4j zhipu适配器不支持视频输入）
 */
@Slf4j
@Service
public class GlmVisionRecognizer implements ContentRecognizer {

    @Autowired
    @Qualifier("visionModel")
    private ChatModel visionModel;

    @Autowired
    @Qualifier("aiModel")
    private ChatModel aiModel;

    @Autowired
    private ZhipuAIConfig zhipuAIConfig;

    @Autowired
    private RestTemplate restTemplate;

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
     * 视频菜谱提取提示词
     */
    private static final String VIDEO_RECIPE_PROMPT = """
        你是一个专业的菜谱信息提取专家。请观看这个视频并从中提取菜谱信息。
        仔细观察视频中的食材、烹饪步骤、调料用量等信息。

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
        1. 先判断视频是否与做菜/烹饪/菜品相关（包括教程、制作过程、食材展示等），只要能从中提取出菜品信息就设置 isRecipe=true；只有完全与做菜无关时才设置 isRecipe=false
        2. 如果内容表面上是食谱格式，但实际并非正经的食品制作（如用烹饪术语做隐晦描述、借食品之名传播其他内容），请设置 isRecipe=false
        3. 仔细观察视频画面中的食材和操作步骤
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
    public String recognize(FetchedContent content) {
        if (!content.isFetchSuccess()) {
            return fallbackRecognize(content);
        }

        // 分支：视频 vs 图片+文字
        if (content.hasVideo()) {
            return recognizeVideo(content);
        } else {
            return recognizeImageAndText(content);
        }
    }

    /**
     * 图片+文字识别（通过 LangChain4j visionModel）
     */
    private String recognizeImageAndText(FetchedContent content) {
        log.info("开始图片+文字识别，图片数量: {}, 文字长度: {}",
            content.getImageBase64List() != null ? content.getImageBase64List().size() : 0,
            content.getTextContent() != null ? content.getTextContent().length() : 0);

        try {
            List<dev.langchain4j.data.message.Content> contents = new ArrayList<>();

            // 构建提示词
            String textContext = content.hasText()
                ? "文字内容：\n" + content.getTextContent()
                : "请根据图片内容提取菜谱信息。";
            String prompt = String.format(RECIPE_EXTRACTION_PROMPT, textContext);
            contents.add(TextContent.from(prompt));

            // 添加图片（最多5张）
            if (content.hasImages()) {
                List<String> images = content.getImageBase64List().stream().limit(5).toList();
                for (String base64 : images) {
                    contents.add(ImageContent.from(base64, "image/jpeg"));
                }
            }

            // 如果没有图片也没有文字，降级
            if (!content.hasImages() && !content.hasText()) {
                return fallbackRecognize(content);
            }

            UserMessage userMessage = UserMessage.from(contents);
            ChatResponse response = visionModel.chat(userMessage);
            String result = response.aiMessage().text();

            log.info("图片+文字识别完成，结果长度: {}", result.length());
            return result;

        } catch (Exception e) {
            log.error("图片+文字识别失败", e);
            return fallbackRecognize(content);
        }
    }

    /**
     * 视频识别（直接调用智谱HTTP API）
     *
     * 原因：langchain4j-community-zhipu-ai 的适配器只处理 TextContent 和 ImageContent，
     * 不处理 VideoContent，因此必须绕过 langchain4j 直接调用HTTP API。
     */
    private String recognizeVideo(FetchedContent content) {
        log.info("开始视频识别，视频URL: {}",
            content.getVideoUrl() != null ? "有直链" : "无直链");

        try {
            Map<String, Object> requestBody = buildVideoApiRequest(content);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + zhipuAIConfig.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 设置较长超时（视频理解耗时较长）
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                zhipuAIConfig.getBaseUrl(),
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map body = response.getBody();
            if (body == null) {
                log.error("智谱API返回空响应");
                return fallbackRecognize(content);
            }

            List<Map> choices = (List<Map>) body.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.error("智谱API返回空choices");
                return fallbackRecognize(content);
            }

            Map message = (Map) choices.get(0).get("message");
            String result = (String) message.get("content");

            log.info("视频识别完成，结果长度: {}", result.length());
            return result;

        } catch (Exception e) {
            log.error("视频识别失败", e);
            // 降级：如果有封面图，用图片识别
            if (content.getCoverImage() != null) {
                log.info("降级为封面图识别");
                return recognizeWithCoverImage(content);
            }
            return fallbackRecognize(content);
        }
    }

    /**
     * 构建智谱视频理解API请求体
     */
    private Map<String, Object> buildVideoApiRequest(FetchedContent content) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", zhipuAIConfig.getVisionModel());
        request.put("max_tokens", 2048);

        List<Map<String, Object>> contentParts = new ArrayList<>();

        // 视频部分（跳过不支持的格式如 .m4s DASH分片）
        if (content.getVideoUrl() != null && !content.getVideoUrl().isEmpty()
            && isSupportedVideoUrl(content.getVideoUrl())) {
            Map<String, Object> videoPart = new HashMap<>();
            videoPart.put("type", "video_url");
            videoPart.put("video_url", Map.of("url", content.getVideoUrl()));
            contentParts.add(videoPart);
        } else if (content.getVideoBase64() != null && !content.getVideoBase64().isEmpty()) {
            Map<String, Object> videoPart = new HashMap<>();
            videoPart.put("type", "video_url");
            videoPart.put("video_url", Map.of("url", content.getVideoBase64()));
            contentParts.add(videoPart);
        }

        // 文字部分
        String prompt = VIDEO_RECIPE_PROMPT;
        if (content.hasText()) {
            prompt = "视频相关文字信息：\n" + content.getTextContent() + "\n\n" + VIDEO_RECIPE_PROMPT;
        }
        contentParts.add(Map.of("type", "text", "text", prompt));

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", contentParts);
        request.put("messages", List.of(message));

        return request;
    }

    /**
     * 检查视频URL是否为GLM Vision支持的格式
     * 不支持的格式：.m4s（B站DASH分片）、.m3u8（HLS切片）
     */
    private boolean isSupportedVideoUrl(String url) {
        String lower = url.toLowerCase();
        return !lower.endsWith(".m4s") && !lower.endsWith(".m3u8");
    }

    /**
     * 用封面图降级识别
     */
    private String recognizeWithCoverImage(FetchedContent content) {
        try {
            List<dev.langchain4j.data.message.Content> contents = new ArrayList<>();
            String prompt = String.format(RECIPE_EXTRACTION_PROMPT,
                "请根据视频封面图片提取菜谱信息。");
            contents.add(TextContent.from(prompt));

            // 封面图作为URL传入（ImageContent支持URL）
            contents.add(ImageContent.from(content.getCoverImage()));

            UserMessage userMessage = UserMessage.from(contents);
            ChatResponse response = visionModel.chat(userMessage);
            return response.aiMessage().text();
        } catch (Exception e) {
            log.error("封面图识别失败", e);
            return fallbackRecognize(content);
        }
    }

    /**
     * 降级识别：仅文字 → agentModel 文本提取
     * 无内容 → URL关键词推断
     */
    private String fallbackRecognize(FetchedContent content) {
        log.info("使用降级识别策略");

        if (content.hasText()) {
            // 有文字内容，用文本模型提取
            String prompt = String.format(TEXT_ONLY_PROMPT, content.getTextContent());
            return aiModel.chat(prompt);
        }

        // 最后降级：URL关键词推断
        String fallbackPrompt = String.format(TEXT_ONLY_PROMPT,
            "请根据以下信息推断可能的菜谱。URL: " +
            (content.getCoverImage() != null ? content.getCoverImage() : "未知来源") +
            "。如果无法推断，请返回 isRecipe=false。");
        return aiModel.chat(fallbackPrompt);
    }
}
