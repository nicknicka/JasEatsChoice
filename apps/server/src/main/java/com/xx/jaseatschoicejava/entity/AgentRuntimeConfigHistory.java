package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 运行时配置版本历史。只记配置值，不含任何密钥。
 */
@Data
@TableName("ai_agent_runtime_config_history")
public class AgentRuntimeConfigHistory {

    @TableId(type = IdType.INPUT)
    private String historyId;

    private String configId;

    private Integer version;

    private String provider;

    private String model;

    private String endpoint;

    private Boolean dataPolicyAck;

    private String updatedBy;

    private LocalDateTime createdAt;
}
