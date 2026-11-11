package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 运行时配置（单例）。不含任何密钥。
 */
@Data
@TableName("ai_agent_runtime_config")
public class AgentRuntimeConfig {

    @TableId(type = IdType.INPUT)
    private String configId = "default";

    private String provider;

    private String model;

    private String endpoint;

    private Boolean dataPolicyAck = false;

    private Integer version = 0;

    private String updatedBy;

    private LocalDateTime updatedAt;
}
