package com.xx.jaseatschoicejava.service.agent;

import com.xx.jaseatschoicejava.entity.AgentChannelConfig;
import com.xx.jaseatschoicejava.entity.AgentEvent;

/**
 * 通知通道端口。通道失败必须被上层隔离，不得影响 dsh Ticket 状态。
 */
public interface NotificationChannel {

    /** 通道编码，与 {@link AgentChannelConfig#getChannelCode()} 一致。 */
    String code();

    /** 投递一个事件。抛出异常表示该通道失败。 */
    void dispatch(AgentEvent event, AgentChannelConfig config);
}
