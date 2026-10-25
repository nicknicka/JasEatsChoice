package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.GroupMember;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群成员关系服务接口
 */
public interface GroupMemberService extends IService<GroupMember> {

    /**
     * 退出群聊
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean leaveGroup(String groupId, String userId);

    /**
     * 移除成员（踢人）
     * @param groupId 群ID
     * @param operatorId 操作者ID（群主或管理员）
     * @param targetUserId 被移除的用户ID
     * @return 是否成功
     */
    boolean removeMember(String groupId, String operatorId, String targetUserId);

    /**
     * 添加成员
     * @param groupId 群ID
     * @param userId 用户ID
     * @param role 角色
     * @return 是否成功
     */
    boolean addMember(String groupId, String userId, String role);

    /**
     * 获取群成员列表
     * @param groupId 群ID
     * @return 成员列表
     */
    List<GroupMember> getGroupMembers(String groupId);

    /**
     * 检查用户是否是群成员
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 是否是成员
     */
    boolean isGroupMember(String groupId, String userId);

    /**
     * 检查用户是否是群主
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 是否是群主
     */
    boolean isGroupOwner(String groupId, String userId);

    /**
     * 获取用户在群中的角色
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 角色，如果不是成员返回null
     */
    String getUserRole(String groupId, String userId);

    /**
     * 获取用户在群的所有加入时间段
     * 用于查询消息时过滤：只显示用户在群期间的消息
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 加入时间列表（按时间升序）
     */
    List<LocalDateTime> getUserJoinTimes(String groupId, String userId);
}
