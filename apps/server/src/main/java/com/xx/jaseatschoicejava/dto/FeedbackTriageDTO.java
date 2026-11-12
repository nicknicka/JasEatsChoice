package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 管理员分诊入参：target 只接受 needs_info / accepted / rejected。
 */
@Data
public class FeedbackTriageDTO {

    private String status;

    /** 接受时是否判定为平台系统问题（决定能否进入 GitHub 桥接）。 */
    private Boolean platformSystem;

    /** 分诊优先级：normal / important / urgent；为空表示不改动。 */
    private String priority;

    /** 指派负责人 sys_admin.admin_id；0 表示取消认领，null 表示不改动。 */
    private Long assigneeId;
}
