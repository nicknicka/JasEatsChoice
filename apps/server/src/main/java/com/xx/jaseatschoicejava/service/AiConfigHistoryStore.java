package com.xx.jaseatschoicejava.service;

/**
 * AI 配置历史独立存储。普通系统配置接口不能访问该存储。
 */
public interface AiConfigHistoryStore {

    String load();

    void save(String historyJson);
}
