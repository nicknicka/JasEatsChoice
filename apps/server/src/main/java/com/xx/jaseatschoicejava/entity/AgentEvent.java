package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代理事件：dsh 的人工介入/状态变化在平台侧的统一表示，不含隐私数据。
 */
@Data
@TableName("ai_agent_event")
public class AgentEvent {

    /** 去重键：同一 dsh 事件重复上报只处理一次。 */
    @TableId(type = IdType.INPUT)
    private String eventId;

    private String eventType;

    private String severity = "info";

    private String ticketId;

    private String ticketStatus;

    private String summary;

    private String actionRequired;

    private String link;

    private LocalDateTime occurredAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
