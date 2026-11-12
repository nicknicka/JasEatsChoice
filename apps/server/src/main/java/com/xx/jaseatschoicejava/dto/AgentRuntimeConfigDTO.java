package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * Agent 运行时配置更新入参。模型 key 不在这个 DTO 里，走独立的只写接口。
 */
@Data
public class AgentRuntimeConfigDTO {

    private String provider;

    private String model;

    private String endpoint;

    private Boolean dataPolicyAck;

    /**
     * 回填来源版本：仅当本次保存是由「回填表单」发起时带上，
     * 用于把这条历史标成 rollback 而不是 manual。
     */
    private Integer sourceVersion;
}
