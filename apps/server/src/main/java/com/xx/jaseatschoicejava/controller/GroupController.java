package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.entity.GroupMember;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.GroupMemberService;
import com.xx.jaseatschoicejava.service.GroupService;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群信息控制器
 */
@Api(tags = "群信息模块")
@RestController
@RequestMapping("/v1/groups")
@Slf4j
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final ChatMsgService chatMsgService;
    private final GroupMemberService groupMemberService;

    /**
     * 创建群
     */
    @ApiOperation("创建群")
    @PostMapping
    public ResponseResult<?> createGroup(@RequestBody Group group) {
        boolean success = groupService.save(group);
        if (success) {
            // ⭐ 将创建者自动添加为群成员（owner角色）
            if (group.getCreatorId() != null) {
                boolean memberAdded = groupMemberService.addMember(
                    group.getId(),
                    group.getCreatorId(),
                    "owner"
                );
                if (!memberAdded) {
                    log.warn("创建群成功，但添加创建者为成员失败: groupId={}, creatorId={}",
                        group.getId(), group.getCreatorId());
                } else {
                    log.info("创建群成功，已自动添加创建者为群成员: groupId={}, creatorId={}",
                        group.getId(), group.getCreatorId());
                }
            }

            // ⭐ 生成并返回 sessionId（用于前端查找会话）
            String sessionId = ChatSessionIdGenerator.getGroupChatSessionId(group.getId());

            // 构建返回数据，包含群信息和 sessionId
            Map<String, Object> result = new HashMap<>();
            result.put("group", group);
            result.put("groupId", group.getId());
            result.put("sessionId", sessionId);

            return ResponseResult.success(result);
        } else {
            return ResponseResult.fail("500", "创建群失败");
        }
    }

    /**
     * 根据群ID或会话ID获取群信息
     */
    @ApiOperation("根据群ID或会话ID获取群信息")
    @GetMapping("/{groupIdOrSessionId}")
    public ResponseResult<?> getGroupById(@PathVariable String groupIdOrSessionId) {
        String groupId = groupIdOrSessionId;

        // 如果传入的是 sessionId（S开头），需要查找对应的 groupId
        if (groupIdOrSessionId != null && groupIdOrSessionId.startsWith("S")) {
            // 通过 sessionId 查询对应的 groupId
            LambdaQueryWrapper<ChatMsg> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMsg::getSessionId, groupIdOrSessionId);
            queryWrapper.eq(ChatMsg::getSessionType, "group");
            queryWrapper.orderByDesc(ChatMsg::getCreateTime);
            queryWrapper.last("LIMIT 1");

            ChatMsg chatMsg = chatMsgService.getOne(queryWrapper);
            if (chatMsg != null && chatMsg.getToId() != null) {
                groupId = chatMsg.getToId();
            } else {
                return ResponseResult.fail("404", "找不到对应的群信息");
            }
        }

        Group group = groupService.getById(groupId);
        if (group != null) {
            return ResponseResult.success(group);
        } else {
            return ResponseResult.fail("404", "群不存在");
        }
    }

    /**
     * 更新群信息
     */
    @ApiOperation("更新群信息")
    @PutMapping("/{groupId}")
    public ResponseResult<?> updateGroup(@PathVariable String groupId, @RequestBody Group group) {
        group.setId(groupId);
        boolean success = groupService.updateById(group);
        if (success) {
            return ResponseResult.success(group);
        } else {
            return ResponseResult.fail("500", "更新群失败");
        }
    }

    /**
     * 删除群
     */
    @ApiOperation("删除群")
    @DeleteMapping("/{groupId}")
    public ResponseResult<?> deleteGroup(@PathVariable String groupId) {
        boolean success = groupService.removeById(groupId);
        if (success) {
            return ResponseResult.success("删除群成功");
        } else {
            return ResponseResult.fail("500", "删除群失败");
        }
    }

    /**
     * 获取我的所有群
     */
    @ApiOperation("获取我的所有群")
    @GetMapping("/my")
    public ResponseResult<?> getMyGroups(@RequestParam String userId) {
        // 获取该用户作为成员的所有群
        List<GroupMember> memberships = groupMemberService.lambdaQuery()
                .eq(GroupMember::getUserId, userId)
                .list();

        List<String> groupIds = memberships.stream()
                .map(GroupMember::getGroupId)
                .toList();

        if (!groupIds.isEmpty()) {
            List<Group> groups = groupService.listByIds(groupIds);
            return ResponseResult.success(groups);
        } else {
            return ResponseResult.success(List.of());
        }
    }

    // ==================== 群成员管理接口 ====================

    /**
     * 退出群聊
     */
    @ApiOperation("退出群聊")
    @DeleteMapping("/{groupId}/leave")
    public ResponseResult<?> leaveGroup(
            @PathVariable String groupId,
            @RequestParam String userId) {
        try {
            boolean success = groupMemberService.leaveGroup(groupId, userId);
            if (success) {
                return ResponseResult.success("退出群聊成功");
            } else {
                return ResponseResult.fail("500", "退出群聊失败");
            }
        } catch (RuntimeException e) {
            return ResponseResult.fail("400", e.getMessage());
        }
    }

    /**
     * 移除成员（踢人）
     */
    @ApiOperation("移除成员")
    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public ResponseResult<?> removeMember(
            @PathVariable String groupId,
            @PathVariable String targetUserId,
            @RequestParam String operatorId) {
        try {
            boolean success = groupMemberService.removeMember(groupId, operatorId, targetUserId);
            if (success) {
                return ResponseResult.success("移除成员成功");
            } else {
                return ResponseResult.fail("500", "移除成员失败");
            }
        } catch (RuntimeException e) {
            return ResponseResult.fail("400", e.getMessage());
        }
    }

    /**
     * 添加成员
     */
    @ApiOperation("添加成员")
    @PostMapping("/{groupId}/members")
    public ResponseResult<?> addMember(
            @PathVariable String groupId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "member") String role) {
        try {
            boolean success = groupMemberService.addMember(groupId, userId, role);
            if (success) {
                return ResponseResult.success("添加成员成功");
            } else {
                return ResponseResult.fail("500", "添加成员失败");
            }
        } catch (RuntimeException e) {
            return ResponseResult.fail("400", e.getMessage());
        }
    }

    /**
     * 获取群成员列表
     */
    @ApiOperation("获取群成员列表")
    @GetMapping("/{groupId}/members")
    public ResponseResult<?> getGroupMembers(@PathVariable String groupId) {
        try {
            List<GroupMember> members = groupMemberService.getGroupMembers(groupId);
            return ResponseResult.success(members);
        } catch (Exception e) {
            log.error("获取群成员列表失败: groupId={}, error={}", groupId, e.getMessage());
            return ResponseResult.fail("500", "获取群成员列表失败");
        }
    }

    /**
     * 检查用户是否是群成员
     */
    @ApiOperation("检查用户是否是群成员")
    @GetMapping("/{groupId}/members/{userId}/check")
    public ResponseResult<?> isGroupMember(
            @PathVariable String groupId,
            @PathVariable String userId) {
        try {
            boolean isMember = groupMemberService.isGroupMember(groupId, userId);
            return ResponseResult.success(Map.of("isMember", isMember));
        } catch (Exception e) {
            log.error("检查群成员失败: groupId={}, userId={}, error={}", groupId, userId, e.getMessage());
            return ResponseResult.fail("500", "检查群成员失败");
        }
    }

    /**
     * 获取用户在群中的角色
     */
    @ApiOperation("获取用户在群中的角色")
    @GetMapping("/{groupId}/members/{userId}/role")
    public ResponseResult<?> getUserRole(
            @PathVariable String groupId,
            @PathVariable String userId) {
        try {
            String role = groupMemberService.getUserRole(groupId, userId);
            if (role != null) {
                return ResponseResult.success(Map.of("role", role));
            } else {
                return ResponseResult.success(Map.of("role", (Object) null));
            }
        } catch (Exception e) {
            log.error("获取用户角色失败: groupId={}, userId={}, error={}", groupId, userId, e.getMessage());
            return ResponseResult.fail("500", "获取用户角色失败");
        }
    }
}
