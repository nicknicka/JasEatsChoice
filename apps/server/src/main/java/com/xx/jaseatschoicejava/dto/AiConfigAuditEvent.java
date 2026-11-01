package com.xx.jaseatschoicejava.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 配置审计事件。摘要只包含非密钥字段和不可逆 key 指纹。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConfigAuditEvent {

    private String action;

    private String operator;

    private String operatedAt;

    private Long oldVersion;

    private Long newVersion;

    private Map<String, Object> oldSummary;

    private Map<String, Object> newSummary;

    private String testResult;

    private String status;

    private String message;
}
