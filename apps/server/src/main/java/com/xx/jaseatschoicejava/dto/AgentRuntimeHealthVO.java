package com.xx.jaseatschoicejava.dto;

import lombok.Data;

import java.util.List;

/**
 * Agent 运行时状态页所需：主机侧健康与硬边界（只读）。
 */
@Data
public class AgentRuntimeHealthVO {

    private Health github;
    private Health dshProfile;
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
