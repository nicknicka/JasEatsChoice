package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.ChatSession;
import com.xx.jaseatschoicejava.entity.GroupMember;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.service.ChatSessionService;
import com.xx.jaseatschoicejava.service.GroupMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.mapper.GroupMemberMapper;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import com.xx.jaseatschoicejava.websocket.WebSocketMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 群成员关系服务实现类
 */
@Slf4j
@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements GroupMemberService {

    private final WebSocketMessageService webSocketMessageService;
    private final UserService userService;
    private final ChatSessionService chatSessionService;

    // 使用 @Lazy 延迟注入以避免循环依赖
    public GroupMemberServiceImpl(@Lazy WebSocketMessageService webSocketMessageService, UserService userService, @Lazy ChatSessionService chatSessionService) {
        this.webSocketMessageService = webSocketMessageService;
        this.userService = userService;
        this.chatSessionService = chatSessionService;
    }

    @Override
    public boolean leaveGroup(String groupId, String userId) {
        log.info("用户退出群聊: groupId={}, userId={}", groupId, userId);

        // 检查用户是否是群主
        String userRole = getUserRole(groupId, userId);
        log.info("用户角色查询结果: groupId={}, userId={}, role={}", groupId, userId, userRole);

        if (isGroupOwner(groupId, userId)) {
            log.warn("群主不能退出群聊，只能解散群: groupId={}, userId={}", groupId, userId);
            throw new RuntimeException("群主不能退出群聊，只能解散群");
        }

        // 先检查是否存在该成员记录
        boolean isMember = isGroupMember(groupId, userId);
        log.info("成员关系检查: groupId={}, userId={}, isMember={}", groupId, userId, isMember);

        if (!isMember) {
            log.warn("用户不是该群的成员，无法退出: groupId={}, userId={}", groupId, userId);
            throw new RuntimeException("用户不是该群的成员");
        }

        // 删除成员关系
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.eq(GroupMember::getUserId, userId);

        log.info("删除条件: groupId={}, userId={}", groupId, userId);

        // 查询要删除的记录（用于调试）
        GroupMember toDelete = getOne(queryWrapper);
        if (toDelete != null) {
            log.info("找到要删除的成员记录: id={}, groupId={}, userId={}, role={}",
                toDelete.getId(), toDelete.getGroupId(), toDelete.getUserId(), toDelete.getRole());
        } else {
            log.error("未找到要删除的成员记录! groupId={}, userId={}", groupId, userId);
        }

        boolean success = remove(queryWrapper);
        if (success) {
            log.info("用户成功退出群聊: groupId={}, userId={}", groupId, userId);

            // ⭐ 新增：删除该用户与该群的会话记录（保留聊天消息记录）
            try {
                String sessionId = ChatSessionIdGenerator.getGroupChatSessionId(groupId);
                LambdaQueryWrapper<ChatSession> sessionQuery = new LambdaQueryWrapper<>();
                sessionQuery.eq(ChatSession::getUserId, userId);
                sessionQuery.eq(ChatSession::getSessionId, sessionId);

                boolean sessionDeleted = chatSessionService.remove(sessionQuery);
                log.info("✅ 删除群会话记录: groupId={}, userId={}, sessionId={}, deleted={}",
                    groupId, userId, sessionId, sessionDeleted);
            } catch (Exception e) {
                log.error("❌ 删除群会话记录失败: groupId={}, userId={}, error={}",
                    groupId, userId, e.getMessage(), e);
            }

            // ⭐ 通知其他群成员
            try {
                // 查询退出用户的信息
                User leaver = userService.getById(userId);
                String leaverName = leaver != null ? leaver.getNickname() : "未知用户";

                // 构造通知数据
                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("userName", leaverName);

                // 异步通知其他群成员（避免阻塞主流程）
                if (webSocketMessageService != null) {
                    new Thread(() -> {
                        try {
                            webSocketMessageService.broadcastToGroup(groupId, "member_left", data);
                            log.info("📢 已通知群成员有用户退出: groupId={}, userId={}", groupId, userId);
                        } catch (Exception e) {
                            log.error("❌ 通知群成员失败: groupId={}, error={}", groupId, e.getMessage(), e);
                        }
                    }).start();
                }
            } catch (Exception e) {
                log.error("❌ 构造退出通知失败: groupId={}, userId={}, error={}", groupId, userId, e.getMessage(), e);
            }
        } else {
            log.error("退出群聊失败: groupId={}, userId={}", groupId, userId);
        }
        return success;
    }

    @Override
    public boolean removeMember(String groupId, String operatorId, String targetUserId) {
        log.info("移除群成员: groupId={}, operatorId={}, targetUserId={}", groupId, operatorId, targetUserId);

        // 检查操作者是否是群主或管理员
        String operatorRole = getUserRole(groupId, operatorId);
        if (!"owner".equals(operatorRole) && !"admin".equals(operatorRole)) {
            throw new RuntimeException("只有群主或管理员才能移除成员");
        }

        // 不能移除群主
        if (isGroupOwner(groupId, targetUserId)) {
            throw new RuntimeException("不能移除群主");
        }

        // 管理员不能移除其他管理员
        String targetRole = getUserRole(groupId, targetUserId);
        if ("admin".equals(targetRole) && !"owner".equals(operatorRole)) {
            throw new RuntimeException("管理员不能移除其他管理员");
        }

        // 删除成员关系
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.eq(GroupMember::getUserId, targetUserId);

        boolean success = remove(queryWrapper);
        if (success) {
            log.info("成功移除群成员: groupId={}, targetUserId={}", groupId, targetUserId);
        }
        return success;
    }

    @Override
    public boolean addMember(String groupId, String userId, String role) {
        log.info("添加群成员: groupId={}, userId={}, role={}", groupId, userId, role);

        // 检查是否已经是成员
        if (isGroupMember(groupId, userId)) {
            log.warn("用户已经是群成员: groupId={}, userId={}", groupId, userId);
            return true;
        }

        // 创建成员关系
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setUserId(userId);
        groupMember.setRole(role != null ? role : "member");
        groupMember.setJoinTime(LocalDateTime.now());

        boolean success = save(groupMember);
        if (success) {
            log.info("成功添加群成员: groupId={}, userId={}", groupId, userId);
        }
        return success;
    }

    @Override
    public List<GroupMember> getGroupMembers(String groupId) {
        log.info("获取群成员列表: groupId={}", groupId);

        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.orderByAsc(GroupMember::getJoinTime);

        return list(queryWrapper);
    }

    @Override
    public boolean isGroupMember(String groupId, String userId) {
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.eq(GroupMember::getUserId, userId);

        return count(queryWrapper) > 0;
    }

    @Override
    public boolean isGroupOwner(String groupId, String userId) {
        return "owner".equals(getUserRole(groupId, userId));
    }

    @Override
    public String getUserRole(String groupId, String userId) {
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.eq(GroupMember::getUserId, userId);

        GroupMember groupMember = getOne(queryWrapper);
        return groupMember != null ? groupMember.getRole() : null;
    }

    @Override
    public List<LocalDateTime> getUserJoinTimes(String groupId, String userId) {
        log.info("获取用户在群的加入时间: groupId={}, userId={}", groupId, userId);

        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, groupId);
        queryWrapper.eq(GroupMember::getUserId, userId);
        queryWrapper.orderByAsc(GroupMember::getJoinTime);

        List<GroupMember> memberships = list(queryWrapper);

        // 提取所有加入时间
        List<LocalDateTime> joinTimes = memberships.stream()
                .map(GroupMember::getJoinTime)
                .collect(Collectors.toList());

        log.info("用户在群的加入时间: groupId={}, userId={}, joinTimes={}", groupId, userId, joinTimes);

        return joinTimes;
    }
}
