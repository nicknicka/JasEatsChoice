package com.xx.jaseatschoicejava.exception;

/**
 * 用户超过 AI 能力配额时抛出。
 */
public class AiQuotaExceededException extends RuntimeException {

    public AiQuotaExceededException() {
        super("请求过于频繁，请稍后再试");
    }
}
