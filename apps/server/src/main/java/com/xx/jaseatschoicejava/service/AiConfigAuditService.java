package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.AiConfigAuditEvent;

/**
 * AI 配置审计服务。
 */
public interface AiConfigAuditService {

    void record(AiConfigAuditEvent event);
}
