package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.util.List;

/**
 * Agent 运行时配置页所需的状态：配置、健康、硬边界与密钥元数据。
 */
@Data
public class AgentRuntimeHealthVO {

    private String provider;
    private String model;
    private String endpoint;
    private Boolean dataPolicyAck;
    private Integer version;
    private String updatedBy;

    /** 模型 key 是否存在（绝不回显明文，只给布尔与更新时间）。 */
    private boolean modelKeyConfigured;
    private String modelKeyUpdatedAt;

    private Health github;
    private Health dshProfile;
    private Health modelHealth;
    private Health isolatedAccount;

    /** 硬边界固定开启，页面无法关闭。 */
    private List<Boundary> boundaries;

    @Data
    public static class Health {
        private String name;
        private boolean ok;
        private String detail;

        public Health(String name, boolean ok, String detail) {
            this.name = name;
            this.ok = ok;
            this.detail = detail;
        }
    }

    @Data
    public static class Boundary {
        private String code;
        private String description;
        private boolean enabled;

        public Boundary(String code, String description, boolean enabled) {
            this.code = code;
            this.description = description;
            this.enabled = enabled;
        }
    }
}
