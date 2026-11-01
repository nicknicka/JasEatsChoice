package com.xx.jaseatschoicejava.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.AiConfigAuditEvent;
import com.xx.jaseatschoicejava.entity.SystemLog;
import com.xx.jaseatschoicejava.service.AiConfigAuditService;
import com.xx.jaseatschoicejava.service.SystemLogService;

import lombok.extern.slf4j.Slf4j;

/**
 * 将 AI 配置事件写入现有系统审计日志。事件本身不携带 key 明文。
 */
@Slf4j
@Service
public class AiConfigAuditServiceImpl implements AiConfigAuditService {

    private final SystemLogService systemLogService;
    private final ObjectMapper objectMapper;

    public AiConfigAuditServiceImpl(SystemLogService systemLogService, ObjectMapper objectMapper) {
        this.systemLogService = systemLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(AiConfigAuditEvent event) {
        SystemLog systemLog = new SystemLog();
        systemLog.setOperationType(event.getAction());
        systemLog.setModule("AI_CONFIG");
        systemLog.setDescription(event.getMessage());
        systemLog.setOperatorName(event.getOperator());
        systemLog.setOperatorType("ADMIN");
        systemLog.setStatus(event.getStatus());
        systemLog.setCreatedAt(event.getOperatedAt() == null
            ? LocalDateTime.now()
            : LocalDateTime.parse(event.getOperatedAt()));
        Map<String, Object> params = new HashMap<>();
        params.put("oldVersion", event.getOldVersion() == null ? 0L : event.getOldVersion());
        params.put("newVersion", event.getNewVersion() == null ? 0L : event.getNewVersion());
        params.put("oldSummary", event.getOldSummary() == null ? "" : maskSensitive(event.getOldSummary()));
        params.put("newSummary", event.getNewSummary() == null ? "" : maskSensitive(event.getNewSummary()));
        params.put("testResult", event.getTestResult() == null ? "" : event.getTestResult());
        systemLog.setParams(toJson(params));
        systemLog.setResult(toJson(Map.of(
            "status", event.getStatus(),
            "testResult", event.getTestResult() == null ? "" : event.getTestResult()
        )));
        systemLogService.saveLog(systemLog);
    }

    private Object maskSensitive(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> masked = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String field = String.valueOf(entry.getKey()).toLowerCase().replace("_", "");
                masked.put(String.valueOf(entry.getKey()),
                    isSensitiveField(field) ? "***" : maskSensitive(entry.getValue()));
            }
            return masked;
        }
        return value;
    }

    private boolean isSensitiveField(String normalizedField) {
        return normalizedField.equals("apikey")
            || normalizedField.equals("key")
            || normalizedField.equals("activekey")
            || normalizedField.equals("runtimekey")
            || normalizedField.equals("secret")
            || normalizedField.equals("token")
            || normalizedField.equals("password")
            || normalizedField.equals("ciphertext");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            log.warn("AI 配置审计序列化失败", exception);
            return "{}";
        }
    }
}
