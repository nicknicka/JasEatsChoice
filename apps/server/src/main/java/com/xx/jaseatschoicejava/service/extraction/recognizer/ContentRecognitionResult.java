package com.xx.jaseatschoicejava.service.extraction.recognizer;

/**
 * 内容识别结果。degradationReason 非空表示成功完成了降级提取，需要向用户说明。
 */
public record ContentRecognitionResult(String response, String degradationReason) {

    public static ContentRecognitionResult success(String response) {
        return new ContentRecognitionResult(response, null);
    }

    public static ContentRecognitionResult degraded(String response, String reason) {
        return new ContentRecognitionResult(response, reason);
    }
}
