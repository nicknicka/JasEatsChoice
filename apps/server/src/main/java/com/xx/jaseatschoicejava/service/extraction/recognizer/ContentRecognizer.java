package com.xx.jaseatschoicejava.service.extraction.recognizer;

import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

/**
 * 内容识别器接口
 * 负责将抓取到的内容交给AI进行识别和结构化
 */
public interface ContentRecognizer {

    /**
     * 识别内容并返回结构化的JSON字符串
     *
     * @param content 抓取到的内容
     * @return AI返回的结构化JSON字符串
     */
    String recognize(FetchedContent content);
}
