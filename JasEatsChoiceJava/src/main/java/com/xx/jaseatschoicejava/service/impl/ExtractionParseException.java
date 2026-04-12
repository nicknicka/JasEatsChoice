package com.xx.jaseatschoicejava.service.impl;

/**
 * 内容提取解析失败异常。
 *
 * 用于区分 AI 已返回但 JSON 解析失败的场景，避免被当作普通任务失败反复重试。
 */
class ExtractionParseException extends RuntimeException {

    private final String rawResponse;

    ExtractionParseException(String message, String rawResponse, Throwable cause) {
        super(message, cause);
        this.rawResponse = rawResponse;
    }

    String getRawResponse() {
        return rawResponse;
    }
}