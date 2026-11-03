package com.xx.jaseatschoicejava.service.extraction.recognizer;

import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

/**
 * 内容识别器接口
 * 负责将抓取到的内容交给AI进行识别和结构化
 */
public interface ContentRecognizer {

    /**
     * 识别内容并返回结构化 JSON，同时携带本次降级说明。
     *
     * @param content 抓取到的内容
     * @return AI 返回的结构化 JSON 与用户可见的降级原因
     */
    ContentRecognitionResult recognize(FetchedContent content);
}
