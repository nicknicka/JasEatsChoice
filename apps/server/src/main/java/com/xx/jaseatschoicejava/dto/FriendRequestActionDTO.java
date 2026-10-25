package com.xx.jaseatschoicejava.dto;

import lombok.Data;

/**
 * 好友请求操作数据传输对象
 * 用于接受/拒绝好友请求
 */
@Data
public class FriendRequestActionDTO {
    /**
     * 当前用户ID（接受者/拒绝者）
     */
    private String userId;

    /**
     * 请求者ID
     */
    private String requesterId;
}
