package com.xx.jaseatschoicejava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 通知通道配置：启停、事件订阅、严重级别、安静时段与接收人。
 */
@Data
@TableName("ai_agent_channel_config")
public class AgentChannelConfig {

    @TableId(type = IdType.INPUT)
    private String channelCode;

    private Boolean enabled = true;

    /** 逗号分隔的事件类型；空表示全部。 */
    private String eventTypes;

    private String minSeverity = "info";

    private String quietStart;

    private String quietEnd;

    /** 逗号分隔的接收人用户ID。 */
    private String recipients;

    private Integer retryLimit = 2;

    private LocalDateTime updatedAt;
}
